package com.evolua.subscription.interfaces.rest;

import com.evolua.subscription.application.PlanCatalogService;
import com.evolua.subscription.application.AdMobSsvVerifier;
import com.evolua.subscription.application.SubscriptionService;
import com.evolua.subscription.config.BillingProperties;
import com.evolua.subscription.domain.AdRewardSession;
import com.evolua.subscription.domain.AiQuotaConsumeResult;
import com.evolua.subscription.domain.AiQuotaStatus;
import com.evolua.subscription.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1")
public class SubscriptionController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "planCode", "status", "billingCycle", "premium", "createdAt", "updatedAt");
  private static final String DEFAULT_SORT_BY = "updatedAt";

  private final SubscriptionService service;
  private final PlanCatalogService planCatalogService;
  private final SubscriptionMapper mapper;
  private final CurrentUserProvider currentUserProvider;
  private final BillingProperties billingProperties;
  private final AdMobSsvVerifier adMobSsvVerifier;

  public SubscriptionController(
      SubscriptionService service,
      PlanCatalogService planCatalogService,
      SubscriptionMapper mapper,
      CurrentUserProvider currentUserProvider,
      BillingProperties billingProperties,
      AdMobSsvVerifier adMobSsvVerifier) {
    this.service = service;
    this.planCatalogService = planCatalogService;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
    this.billingProperties = billingProperties;
    this.adMobSsvVerifier = adMobSsvVerifier;
  }

  @GetMapping("/plans")
  @Operation(summary = "List active plans")
  public ResponseEntity<ApiResponse<java.util.List<PlanViewResponse>>> plans() {
    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Listed",
            planCatalogService.listActivePlans().stream().map(mapper::toPlanResponse).toList()));
  }

  @GetMapping("/subscription/current")
  @Operation(summary = "Current subscription")
  public ResponseEntity<ApiResponse<CurrentSubscriptionResponse>> current() {
    var userId = currentUserId();
    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Current subscription",
            mapper.toCurrentResponse(
                service.current(userId),
                service.quotaStatus(userId, "AI_ACTION"),
                service.mentorPremiumPassStatus(userId))));
  }

  @GetMapping("/subscriptions")
  @Operation(summary = "Subscription history")
  public ResponseEntity<ApiResponse<PageResponse<CurrentSubscriptionResponse>>> history(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Boolean premium) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (status != null && !status.isBlank()) {
      filters.put("status", status.trim());
    }
    if (premium != null) {
      filters.put("premium", premium);
    }

    var result =
        service.history(
            currentUserId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            status == null ? null : status.trim(),
            premium);

    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Listed",
            PageResponse.from(
                result,
                mapper::toCurrentResponse,
                query.effectiveSortBy(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
                query.normalizedSortDir(),
                filters)));
  }

  @PostMapping("/billing/checkout")
  @Operation(summary = "Start checkout")
  public ResponseEntity<ApiResponse<BillingCheckoutResponse>> startCheckout(
      @Valid @RequestBody BillingCheckoutRequest request) {
    var checkout = service.startCheckout(currentUserId(), request.planCode().trim(), request.frontendBaseUrl());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Checkout created", mapper.toCheckoutResponse(checkout)));
  }

  @GetMapping("/billing/checkout/{checkoutId}")
  @Operation(summary = "Get checkout status")
  public ResponseEntity<ApiResponse<BillingCheckoutResponse>> checkoutStatus(
      @PathVariable String checkoutId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Checkout status",
            mapper.toCheckoutResponse(service.checkoutStatus(currentUserId(), checkoutId))));
  }

  @PostMapping("/subscription/cancel")
  @Operation(summary = "Cancel current premium subscription")
  public ResponseEntity<ApiResponse<CurrentSubscriptionResponse>> cancel() {
    return ResponseEntity.ok(
        ApiResponse.success(
            200, "Subscription canceled", mapper.toCurrentResponse(service.cancel(currentUserId()))));
  }

  @GetMapping("/internal/subscription/access")
  @Operation(summary = "Internal access summary")
  public ResponseEntity<ApiResponse<SubscriptionAccessResponse>> access(
      @RequestParam String userId, @RequestHeader(name = "X-Internal-Token", required = false) String token) {
    validateInternalToken(token);
    return ResponseEntity.ok(
        ApiResponse.success(200, "Access summary", mapper.toAccessResponse(service.accessSummary(userId))));
  }

  @PostMapping("/internal/ai-quota/consume")
  @Operation(summary = "Consume AI quota for an internal caller")
  public ResponseEntity<ApiResponse<AiQuotaConsumeResponse>> consumeAiQuota(
      @RequestHeader(name = "X-Internal-Token", required = false) String token,
      @Valid @RequestBody AiQuotaConsumeRequest request) {
    validateInternalToken(token);
    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "AI quota consumed",
            AiQuotaConsumeResponse.from(service.consumeAiQuota(request.userId(), request.resource()))));
  }

  @GetMapping("/internal/ai-quota/status")
  @Operation(summary = "AI quota status for an internal caller")
  public ResponseEntity<ApiResponse<AiQuotaStatusResponse>> aiQuotaStatus(
      @RequestParam String userId,
      @RequestParam(defaultValue = "AI_ACTION") String resource,
      @RequestHeader(name = "X-Internal-Token", required = false) String token) {
    validateInternalToken(token);
    return ResponseEntity.ok(
        ApiResponse.success(200, "AI quota status", AiQuotaStatusResponse.from(service.quotaStatus(userId, resource))));
  }

  @PostMapping("/internal/ad-rewards/session")
  @Operation(summary = "Create rewarded ad session for an internal caller")
  public ResponseEntity<ApiResponse<AdRewardSessionResponse>> createInternalAdRewardSession(
      @RequestHeader(name = "X-Internal-Token", required = false) String token,
      @Valid @RequestBody AdRewardSessionRequest request) {
    validateInternalToken(token);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                201,
                "Reward session created",
                AdRewardSessionResponse.from(service.createRewardSession(request.userId(), request.rewardType()))));
  }

  @PostMapping("/ads/reward-session")
  @Operation(summary = "Create rewarded ad session for the authenticated user")
  public ResponseEntity<ApiResponse<AdRewardSessionResponse>> createAdRewardSession(
      @Valid @RequestBody PublicAdRewardSessionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                201,
                "Reward session created",
                AdRewardSessionResponse.from(
                    service.createRewardSession(currentUserId(), request.rewardType()))));
  }

  @GetMapping("/public/ads/admob/reward-callback")
  @Operation(summary = "AdMob rewarded ad server-side verification callback")
  public ResponseEntity<ApiResponse<AdRewardSessionResponse>> adMobRewardCallback(
      HttpServletRequest request,
      @RequestParam(name = "custom_data", required = false) String customData,
      @RequestParam(name = "transaction_id", required = false) String transactionId) {
    if (customData == null || customData.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing reward session custom_data");
    }
    try {
      adMobSsvVerifier.verify(request.getQueryString());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
    }
    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Reward granted",
            AdRewardSessionResponse.from(service.grantAdMobReward(customData.trim(), transactionId))));
  }

  @PostMapping("/public/billing/mercadopago/webhook")
  @Operation(summary = "Mercado Pago webhook")
  public ResponseEntity<ApiResponse<BillingCheckoutResponse>> mercadoPagoWebhook(
      @RequestBody Map<String, Object> payload,
      @RequestHeader(name = "x-signature", required = false) String signature) {
    validateWebhookSignature(signature);
    return ResponseEntity.ok(
        ApiResponse.success(
            200, "Webhook processed", mapper.toCheckoutResponse(service.processWebhook(payload))));
  }

  private String currentUserId() {
    return currentUserProvider.getCurrentUser().userId();
  }

  private void validateInternalToken(String token) {
    var expected = billingProperties.getInternalToken();
    if (expected == null || expected.isBlank() || !expected.equals(token)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal token");
    }
  }

  private void validateWebhookSignature(String signature) {
    var expected = billingProperties.getMercadoPago().getWebhookSecret();
    if (expected == null || expected.isBlank()) {
      return;
    }
    if (signature == null || signature.isBlank()) {
      return;
    }
    if (!signature.equals(expected) && !signature.contains(expected)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid webhook signature");
    }
  }

  public record AiQuotaConsumeRequest(
      @jakarta.validation.constraints.NotBlank String userId,
      @jakarta.validation.constraints.NotBlank String resource) {}

  public record AiQuotaConsumeResponse(Boolean allowed, String consumptionType, AiQuotaStatusResponse status) {
    static AiQuotaConsumeResponse from(AiQuotaConsumeResult result) {
      return new AiQuotaConsumeResponse(result.allowed(), result.consumptionType(), AiQuotaStatusResponse.from(result.status()));
    }
  }

  public record AiQuotaStatusResponse(
      String userId,
      String resource,
      Boolean premium,
      String planCode,
      Integer dailyLimit,
      Integer usedToday,
      Integer remainingToday,
      Integer rewardedCreditsGrantedToday,
      Integer rewardedCreditsUsedToday,
      Boolean rewardedAdAvailable,
      Boolean upgradeRecommended,
      String limitMessage) {
    static AiQuotaStatusResponse from(AiQuotaStatus status) {
      return new AiQuotaStatusResponse(
          status.userId(),
          status.resource(),
          status.premium(),
          status.planCode(),
          status.dailyLimit(),
          status.usedToday(),
          status.remainingToday(),
          status.rewardedCreditsGrantedToday(),
          status.rewardedCreditsUsedToday(),
          status.rewardedAdAvailable(),
          status.upgradeRecommended(),
          status.limitMessage());
    }
  }

  public record AdRewardSessionRequest(
      @jakarta.validation.constraints.NotBlank String userId,
      @jakarta.validation.constraints.NotBlank String rewardType) {}

  public record PublicAdRewardSessionRequest(
      @jakarta.validation.constraints.NotBlank String rewardType) {}

  public record AdRewardSessionResponse(
      String id,
      String provider,
      String rewardType,
      String status,
      String customData,
      java.time.Instant expiresAt,
      java.time.Instant grantedAt) {
    static AdRewardSessionResponse from(AdRewardSession session) {
      return new AdRewardSessionResponse(
          session.publicId(),
          session.provider(),
          session.rewardType(),
          session.status(),
          session.publicId(),
          session.expiresAt(),
          session.grantedAt());
    }
  }
}
