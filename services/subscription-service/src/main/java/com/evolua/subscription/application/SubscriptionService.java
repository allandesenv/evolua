package com.evolua.subscription.application;

import com.evolua.subscription.config.BillingProperties;
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
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {
  private final SubscriptionRepository subscriptionRepository;
  private final BillingCheckoutRepository billingCheckoutRepository;
  private final BillingEventRepository billingEventRepository;
  private final PlanCatalogService planCatalogService;
  private final MercadoPagoBillingClient mercadoPagoBillingClient;
  private final BillingProperties billingProperties;
  private final ObjectMapper objectMapper;

  public SubscriptionService(
      SubscriptionRepository subscriptionRepository,
      BillingCheckoutRepository billingCheckoutRepository,
      BillingEventRepository billingEventRepository,
      PlanCatalogService planCatalogService,
      MercadoPagoBillingClient mercadoPagoBillingClient,
      BillingProperties billingProperties,
      ObjectMapper objectMapper) {
    this.subscriptionRepository = subscriptionRepository;
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
    return response;
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
