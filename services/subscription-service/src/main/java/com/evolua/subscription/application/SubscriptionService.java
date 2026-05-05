package com.evolua.subscription.application;

import com.evolua.subscription.config.BillingProperties;
import com.evolua.subscription.domain.AdRewardSession;
import com.evolua.subscription.domain.AdRewardSessionRepository;
import com.evolua.subscription.domain.AiQuotaConsumeResult;
import com.evolua.subscription.domain.AiQuotaStatus;
import com.evolua.subscription.domain.AiUsageLedger;
import com.evolua.subscription.domain.AiUsageLedgerRepository;
import com.evolua.subscription.domain.BillingCheckout;
import com.evolua.subscription.domain.BillingCheckoutRepository;
import com.evolua.subscription.domain.BillingEvent;
import com.evolua.subscription.domain.BillingEventRepository;
import com.evolua.subscription.domain.PlanCatalogItem;
import com.evolua.subscription.domain.Subscription;
import com.evolua.subscription.domain.SubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {
  private static final String AI_ACTION_RESOURCE = "AI_ACTION";
  private static final int FREE_DAILY_AI_LIMIT = 1;
  private static final int FREE_DAILY_REWARD_LIMIT = 1;
  private static final int PREMIUM_DAILY_AI_LIMIT = 10;

  private final SubscriptionRepository subscriptionRepository;
  private final AiUsageLedgerRepository aiUsageLedgerRepository;
  private final AdRewardSessionRepository adRewardSessionRepository;
  private final BillingCheckoutRepository billingCheckoutRepository;
  private final BillingEventRepository billingEventRepository;
  private final PlanCatalogService planCatalogService;
  private final MercadoPagoBillingClient mercadoPagoBillingClient;
  private final BillingProperties billingProperties;
  private final ObjectMapper objectMapper;

  public SubscriptionService(
      SubscriptionRepository subscriptionRepository,
      AiUsageLedgerRepository aiUsageLedgerRepository,
      AdRewardSessionRepository adRewardSessionRepository,
      BillingCheckoutRepository billingCheckoutRepository,
      BillingEventRepository billingEventRepository,
      PlanCatalogService planCatalogService,
      MercadoPagoBillingClient mercadoPagoBillingClient,
      BillingProperties billingProperties,
      ObjectMapper objectMapper) {
    this.subscriptionRepository = subscriptionRepository;
    this.aiUsageLedgerRepository = aiUsageLedgerRepository;
    this.adRewardSessionRepository = adRewardSessionRepository;
    this.billingCheckoutRepository = billingCheckoutRepository;
    this.billingEventRepository = billingEventRepository;
    this.planCatalogService = planCatalogService;
    this.mercadoPagoBillingClient = mercadoPagoBillingClient;
    this.billingProperties = billingProperties;
    this.objectMapper = objectMapper;
  }

  public Page<Subscription> history(
      String userId, Pageable pageable, String search, String status, Boolean premium) {
    return subscriptionRepository.findAllByUserId(userId, pageable, search, status, premium);
  }

  public Subscription current(String userId) {
    return subscriptionRepository.findCurrentByUserId(userId);
  }

  public boolean hasPremiumAccess(String userId) {
    var current = current(userId);
    return current != null
        && Boolean.TRUE.equals(current.premium())
        && "ACTIVE".equalsIgnoreCase(current.status());
  }

  public BillingCheckout startCheckout(String userId, String planCode, String frontendBaseUrl) {
    var plan = planCatalogService.findRequired(planCode);
    var checkoutBaseUrl = normalizeFrontendBaseUrl(frontendBaseUrl);

    if (!plan.premium()) {
      activateFreePlan(userId, plan);
      return billingCheckoutRepository.save(
          new BillingCheckout(
              null,
              UUID.randomUUID().toString(),
              userId,
              plan.planCode(),
              plan.billingCycle(),
              "MANUAL",
              "ACTIVE",
              false,
              null,
              null,
              null,
              null,
              Instant.now(),
              Instant.now(),
              Instant.now()));
    }

    ensureMercadoPagoConfigured();
    var publicId = UUID.randomUUID().toString();
    var baseReturn = checkoutBaseUrl + "/home?billingCheckoutId=" + publicId;
    var preference =
        mercadoPagoBillingClient.createCheckoutPreference(
            publicId,
            plan,
            baseReturn + "&billingReturn=approved",
            baseReturn + "&billingReturn=pending",
            baseReturn + "&billingReturn=failure",
            billingProperties.getPublicBaseUrl().replaceAll("/$", "")
                + "/v1/public/billing/mercadopago/webhook");

    var now = Instant.now();
    billingCheckoutRepository.save(
        new BillingCheckout(
            null,
            publicId,
            userId,
            plan.planCode(),
            plan.billingCycle(),
            "MERCADO_PAGO",
            "PENDING_PAYMENT",
            true,
            preference.preferenceId(),
            null,
            preference.checkoutUrl(),
            null,
            now,
            now,
            null));

    subscriptionRepository.save(
        new Subscription(
            null,
            userId,
            plan.planCode(),
            "PENDING_PAYMENT",
            plan.billingCycle(),
            false,
            "MERCADO_PAGO",
            null,
            null,
            null,
            null,
            null,
            now,
            now));

    return requireCheckout(publicId);
  }

  public BillingCheckout checkoutStatus(String userId, String checkoutId) {
    var checkout = requireCheckout(checkoutId);
    if (!checkout.userId().equals(userId)) {
      throw new IllegalArgumentException("Checkout not found for authenticated user");
    }
    return checkout;
  }

  public Subscription cancel(String userId) {
    var current = current(userId);
    if (current != null) {
      subscriptionRepository.save(
          new Subscription(
              current.id(),
              current.userId(),
              current.planCode(),
              "CANCELED",
              current.billingCycle(),
              false,
              current.provider(),
              current.providerCustomerId(),
              current.providerPaymentId(),
              current.providerSubscriptionId(),
              current.currentPeriodEndsAt(),
              Instant.now(),
              current.createdAt(),
              Instant.now()));
    }
    return activateFreePlan(userId, planCatalogService.findRequired("essential-free"));
  }

  public BillingCheckout processWebhook(Map<String, Object> payload) {
    ensureMercadoPagoConfigured();
    var providerEventId = extractProviderEventId(payload);
    if (billingEventRepository.existsByProviderAndProviderEventId("MERCADO_PAGO", providerEventId)) {
      var checkoutId = extractCheckoutId(payload);
      return checkoutId == null ? null : billingCheckoutRepository.findByPublicId(checkoutId);
    }

    var payment = mercadoPagoBillingClient.fetchPayment(extractPaymentId(payload));
    if (payment.checkoutPublicId() == null || payment.checkoutPublicId().isBlank()) {
      throw new IllegalArgumentException("Mercado Pago payment is missing external reference");
    }

    billingEventRepository.save(
        new BillingEvent(
            null,
            "MERCADO_PAGO",
            providerEventId,
            payment.status(),
            payment.checkoutPublicId(),
            writePayload(payload),
            Instant.now()));

    var updated = applyPaymentToCheckout(requireCheckout(payment.checkoutPublicId()), payment);
    return billingCheckoutRepository.save(updated);
  }

  public Map<String, Object> accessSummary(String userId) {
    var current = current(userId);
    var response = new LinkedHashMap<String, Object>();
    response.put("userId", userId);
    response.put("premium", hasPremiumAccess(userId));
    response.put("status", current == null ? "NONE" : current.status());
    response.put("planCode", current == null ? "essential-free" : current.planCode());
    var quota = quotaStatus(userId, AI_ACTION_RESOURCE);
    response.put("adsEnabled", !Boolean.TRUE.equals(quota.premium()));
    response.put("aiQuotaRemainingToday", quota.remainingToday());
    return response;
  }

  @Transactional
  public AiQuotaConsumeResult consumeAiQuota(String userId, String resource) {
    var normalizedResource = normalizeResource(resource);
    var premium = hasPremiumAccess(userId);
    var ledger = todayLedger(userId, normalizedResource);
    var limit = premium ? PREMIUM_DAILY_AI_LIMIT : FREE_DAILY_AI_LIMIT;

    if (ledger.baseUsed() < limit) {
      var updated = saveLedger(
          new AiUsageLedger(
              ledger.id(),
              ledger.userId(),
              ledger.resource(),
              ledger.usageDate(),
              ledger.baseUsed() + 1,
              ledger.rewardUsed(),
              ledger.rewardGranted(),
              ledger.createdAt(),
              Instant.now()));
      return new AiQuotaConsumeResult(
          true, premium ? "PREMIUM_DAILY" : "FREE_DAILY", quotaStatusFromLedger(updated, premium));
    }

    if (!premium && ledger.rewardUsed() < ledger.rewardGranted()) {
      var updated = saveLedger(
          new AiUsageLedger(
              ledger.id(),
              ledger.userId(),
              ledger.resource(),
              ledger.usageDate(),
              ledger.baseUsed(),
              ledger.rewardUsed() + 1,
              ledger.rewardGranted(),
              ledger.createdAt(),
              Instant.now()));
      return new AiQuotaConsumeResult(true, "REWARDED_AD", quotaStatusFromLedger(updated, false));
    }

    return new AiQuotaConsumeResult(false, "DENIED", quotaStatusFromLedger(ledger, premium));
  }

  @Transactional
  public AiQuotaStatus quotaStatus(String userId, String resource) {
    var normalizedResource = normalizeResource(resource);
    return quotaStatusFromLedger(todayLedger(userId, normalizedResource), hasPremiumAccess(userId));
  }

  @Transactional
  public AdRewardSession createRewardSession(String userId, String rewardType) {
    var quota = quotaStatus(userId, normalizeResource(rewardType));
    if (Boolean.TRUE.equals(quota.premium())) {
      throw new IllegalArgumentException("Premium users do not need rewarded ads");
    }
    if (!Boolean.TRUE.equals(quota.rewardedAdAvailable())) {
      throw new IllegalArgumentException("Rewarded ad limit reached for today");
    }

    var now = Instant.now();
    return adRewardSessionRepository.save(
        new AdRewardSession(
            null,
            UUID.randomUUID().toString(),
            userId,
            "ADMOB",
            normalizeResource(rewardType),
            "CREATED",
            null,
            now.plus(30, ChronoUnit.MINUTES),
            null,
            now,
            now));
  }

  @Transactional
  public AdRewardSession grantAdMobReward(String sessionPublicId, String transactionId) {
    var session = adRewardSessionRepository.findByPublicId(sessionPublicId);
    if (session == null) {
      throw new IllegalArgumentException("Reward session not found");
    }
    if ("GRANTED".equalsIgnoreCase(session.status())) {
      return session;
    }
    if (session.expiresAt().isBefore(Instant.now())) {
      var expired =
          adRewardSessionRepository.save(
              new AdRewardSession(
                  session.id(),
                  session.publicId(),
                  session.userId(),
                  session.provider(),
                  session.rewardType(),
                  "EXPIRED",
                  session.providerTransactionId(),
                  session.expiresAt(),
                  session.grantedAt(),
                  session.createdAt(),
                  Instant.now()));
      return expired;
    }
    var normalizedTransactionId =
        transactionId == null || transactionId.isBlank() ? session.publicId() : transactionId.trim();
    if (adRewardSessionRepository.existsGrantedByUserIdAndRewardTypeAndProviderTransactionId(
        session.userId(), session.rewardType(), normalizedTransactionId)) {
      return session;
    }

    var ledger = todayLedger(session.userId(), session.rewardType());
    if (ledger.rewardGranted() >= FREE_DAILY_REWARD_LIMIT) {
      return session;
    }
    saveLedger(
        new AiUsageLedger(
            ledger.id(),
            ledger.userId(),
            ledger.resource(),
            ledger.usageDate(),
            ledger.baseUsed(),
            ledger.rewardUsed(),
            ledger.rewardGranted() + 1,
            ledger.createdAt(),
            Instant.now()));

    return adRewardSessionRepository.save(
        new AdRewardSession(
            session.id(),
            session.publicId(),
            session.userId(),
            session.provider(),
            session.rewardType(),
            "GRANTED",
            normalizedTransactionId,
            session.expiresAt(),
            Instant.now(),
            session.createdAt(),
            Instant.now()));
  }

  private BillingCheckout applyPaymentToCheckout(
      BillingCheckout checkout, MercadoPagoBillingClient.MercadoPagoPayment payment) {
    var now = Instant.now();
    if ("approved".equalsIgnoreCase(payment.status())) {
      var plan = planCatalogService.findRequired(checkout.planCode());
      subscriptionRepository.save(
          new Subscription(
              null,
              checkout.userId(),
              plan.planCode(),
              "ACTIVE",
              plan.billingCycle(),
              true,
              "MERCADO_PAGO",
              payment.customerId(),
              payment.paymentId(),
              null,
              now.plus(plan.billingCycle().equalsIgnoreCase("YEARLY") ? 365 : 30, ChronoUnit.DAYS),
              null,
              now,
              now));
      return new BillingCheckout(
          checkout.id(),
          checkout.publicId(),
          checkout.userId(),
          checkout.planCode(),
          checkout.billingCycle(),
          checkout.provider(),
          "APPROVED",
          checkout.premium(),
          checkout.providerPreferenceId(),
          payment.paymentId(),
          checkout.checkoutUrl(),
          null,
          checkout.createdAt(),
          now,
          now);
    }

    return new BillingCheckout(
        checkout.id(),
        checkout.publicId(),
        checkout.userId(),
        checkout.planCode(),
        checkout.billingCycle(),
        checkout.provider(),
        "REJECTED",
        checkout.premium(),
        checkout.providerPreferenceId(),
        payment.paymentId(),
        checkout.checkoutUrl(),
        payment.status(),
        checkout.createdAt(),
        now,
        null);
  }

  private Subscription activateFreePlan(String userId, PlanCatalogItem plan) {
    var now = Instant.now();
    return subscriptionRepository.save(
        new Subscription(
            null,
            userId,
            plan.planCode(),
            "ACTIVE",
            plan.billingCycle(),
            false,
            "MANUAL",
            null,
            null,
            null,
            null,
            null,
            now,
            now));
  }

  private AiUsageLedger todayLedger(String userId, String resource) {
    var today = LocalDate.now(ZoneOffset.UTC);
    var existing = aiUsageLedgerRepository.findByUserIdAndResourceAndUsageDate(userId, resource, today);
    if (existing != null) {
      return existing;
    }
    var now = Instant.now();
    return aiUsageLedgerRepository.save(
        new AiUsageLedger(null, userId, resource, today, 0, 0, 0, now, now));
  }

  private AiUsageLedger saveLedger(AiUsageLedger ledger) {
    return aiUsageLedgerRepository.save(ledger);
  }

  private AiQuotaStatus quotaStatusFromLedger(AiUsageLedger ledger, boolean premium) {
    var current = current(ledger.userId());
    var limit = premium ? PREMIUM_DAILY_AI_LIMIT : FREE_DAILY_AI_LIMIT;
    var rewardRemaining = premium ? 0 : Math.max(0, ledger.rewardGranted() - ledger.rewardUsed());
    var baseRemaining = Math.max(0, limit - ledger.baseUsed());
    var remaining = baseRemaining + rewardRemaining;
    var rewardedAdAvailable =
        !premium
            && ledger.rewardGranted() < FREE_DAILY_REWARD_LIMIT
            && ledger.rewardUsed() >= ledger.rewardGranted()
            && baseRemaining == 0;
    var limitMessage =
        remaining > 0
            ? null
            : premium
                ? "Seu limite diario de IA premium foi atingido. Tente novamente amanha."
                : "Seu limite gratuito de IA acabou por hoje. Assista a um anuncio para liberar +1 analise ou assine Premium.";
    return new AiQuotaStatus(
        ledger.userId(),
        ledger.resource(),
        premium,
        current == null ? "essential-free" : current.planCode(),
        limit,
        ledger.baseUsed() + ledger.rewardUsed(),
        remaining,
        ledger.rewardGranted(),
        ledger.rewardUsed(),
        rewardedAdAvailable,
        !premium,
        limitMessage);
  }

  private String normalizeResource(String resource) {
    if (resource == null || resource.isBlank()) {
      return AI_ACTION_RESOURCE;
    }
    return resource.trim().toUpperCase();
  }

  private BillingCheckout requireCheckout(String checkoutId) {
    var checkout = billingCheckoutRepository.findByPublicId(checkoutId);
    if (checkout == null) {
      throw new IllegalArgumentException("Checkout not found");
    }
    return checkout;
  }

  private void ensureMercadoPagoConfigured() {
    if (billingProperties.getMercadoPago() == null
        || billingProperties.getMercadoPago().getAccessToken() == null
        || billingProperties.getMercadoPago().getAccessToken().isBlank()) {
      throw new IllegalArgumentException("Mercado Pago is not configured");
    }
    if (billingProperties.getPublicBaseUrl() == null || billingProperties.getPublicBaseUrl().isBlank()) {
      throw new IllegalArgumentException("Billing public base URL is not configured");
    }
  }

  private String normalizeFrontendBaseUrl(String rawFrontendBaseUrl) {
    var candidate =
        rawFrontendBaseUrl == null || rawFrontendBaseUrl.isBlank()
            ? billingProperties.getFrontendBaseUrl()
            : rawFrontendBaseUrl.trim();
    if (candidate == null || candidate.isBlank()) {
      throw new IllegalArgumentException("Frontend base URL is not configured");
    }
    if (!candidate.startsWith("http://") && !candidate.startsWith("https://")) {
      throw new IllegalArgumentException("frontendBaseUrl must use http or https");
    }
    return candidate.replaceAll("/$", "");
  }

  private String extractProviderEventId(Map<String, Object> payload) {
    var eventId = payload.get("id");
    return eventId == null ? UUID.randomUUID().toString() : String.valueOf(eventId);
  }

  private Long extractPaymentId(Map<String, Object> payload) {
    var data = payload.get("data");
    if (data instanceof Map<?, ?> map && map.get("id") != null) {
      return Long.valueOf(String.valueOf(map.get("id")));
    }
    if (payload.get("resource") != null) {
      var parts = String.valueOf(payload.get("resource")).split("/");
      return Long.valueOf(parts[parts.length - 1]);
    }
    throw new IllegalArgumentException("Mercado Pago webhook did not include payment id");
  }

  private String extractCheckoutId(Map<String, Object> payload) {
    var externalReference = payload.get("external_reference");
    return externalReference == null ? null : String.valueOf(externalReference);
  }

  private String writePayload(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      return "{}";
    }
  }
}
