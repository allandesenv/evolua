package com.evolua.subscription.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.evolua.subscription.config.BillingProperties;
import com.evolua.subscription.domain.AdRewardSession;
import com.evolua.subscription.domain.AdRewardSessionRepository;
import com.evolua.subscription.domain.AiUsageLedger;
import com.evolua.subscription.domain.AiUsageLedgerRepository;
import com.evolua.subscription.domain.BillingCheckoutRepository;
import com.evolua.subscription.domain.BillingEventRepository;
import com.evolua.subscription.domain.RewardEntitlement;
import com.evolua.subscription.domain.RewardEntitlementRepository;
import com.evolua.subscription.domain.Subscription;
import com.evolua.subscription.domain.SubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SubscriptionServiceQuotaTest {
  @Test
  void freeUserConsumesOneAiActionThenRequiresRewardOrPremium() {
    var service = serviceFor(null);

    var first = service.consumeAiQuota("free-user", "AI_ACTION");
    var second = service.consumeAiQuota("free-user", "AI_ACTION");

    assertTrue(first.allowed());
    assertEquals(0, first.status().remainingToday());
    assertFalse(second.allowed());
    assertTrue(second.status().rewardedAdAvailable());
    assertTrue(second.status().upgradeRecommended());
  }

  @Test
  void rewardedAdGrantsExactlyOneExtraFreeAiActionPerDay() {
    var service = serviceFor(null);
    service.consumeAiQuota("free-user", "AI_ACTION");

    var session = service.createRewardSession("free-user", "AI_ACTION");
    var granted = service.grantAdMobReward(session.publicId(), "tx-1");
    var duplicate = service.grantAdMobReward(session.publicId(), "tx-1");
    var rewardedUse = service.consumeAiQuota("free-user", "AI_ACTION");
    var blocked = service.consumeAiQuota("free-user", "AI_ACTION");

    assertEquals("GRANTED", granted.status());
    assertEquals("GRANTED", duplicate.status());
    assertTrue(rewardedUse.allowed());
    assertEquals("REWARDED_AD", rewardedUse.consumptionType());
    assertFalse(blocked.allowed());
    assertThrows(IllegalArgumentException.class, () -> service.createRewardSession("free-user", "AI_ACTION"));
  }

  @Test
  void premiumUserGetsTenAiActionsWithoutRewardedAds() {
    var premium =
        new Subscription(
            1L,
            "premium-user",
            "premium-monthly",
            "ACTIVE",
            "MONTHLY",
            true,
            "MERCADO_PAGO",
            null,
            null,
            null,
            null,
            null,
            java.time.Instant.now(),
            java.time.Instant.now());
    var service = serviceFor(premium);

    for (var index = 0; index < 10; index++) {
      assertTrue(service.consumeAiQuota("premium-user", "AI_ACTION").allowed());
    }
    var blocked = service.consumeAiQuota("premium-user", "AI_ACTION");

    assertFalse(blocked.allowed());
    assertFalse(blocked.status().rewardedAdAvailable());
    assertFalse(blocked.status().upgradeRecommended());
  }

  @Test
  void rewardedAdGrantsDailyMentorPremiumPassWithoutConsumingAiQuota() {
    var service = serviceFor(null);

    var session = service.createRewardSession("free-user", "MENTOR_PREMIUM_PASS");
    var granted = service.grantAdMobReward(session.publicId(), "mentor-tx-1");
    var duplicate = service.grantAdMobReward(session.publicId(), "mentor-tx-1");
    var pass = service.mentorPremiumPassStatus("free-user");
    var aiAction = service.consumeAiQuota("free-user", "AI_ACTION");

    assertEquals("GRANTED", granted.status());
    assertEquals("GRANTED", duplicate.status());
    assertTrue(pass.active());
    assertFalse(pass.rewardedAdAvailable());
    assertTrue(aiAction.allowed());
    assertEquals("FREE_DAILY", aiAction.consumptionType());
    assertThrows(
        IllegalArgumentException.class,
        () -> service.createRewardSession("free-user", "MENTOR_PREMIUM_PASS"));
  }

  @Test
  void accessSummaryIncludesMentorPassFields() {
    var service = serviceFor(null);
    var session = service.createRewardSession("free-user", "MENTOR_PREMIUM_PASS");
    service.grantAdMobReward(session.publicId(), "mentor-tx-2");

    var summary = service.accessSummary("free-user");

    assertEquals(Boolean.TRUE, summary.get("mentorPremiumPassActive"));
    assertTrue(summary.get("mentorPremiumPassEndsAt") instanceof Instant);
    assertEquals(Boolean.FALSE, summary.get("mentorRewardedAdAvailable"));
  }

  private SubscriptionService serviceFor(Subscription currentSubscription) {
    var subscriptionRepository = mock(SubscriptionRepository.class);
    when(subscriptionRepository.findCurrentByUserId("free-user")).thenReturn(currentSubscription);
    when(subscriptionRepository.findCurrentByUserId("premium-user")).thenReturn(currentSubscription);
    return new SubscriptionService(
        subscriptionRepository,
        new InMemoryAiUsageLedgerRepository(),
        new InMemoryAdRewardSessionRepository(),
        new InMemoryRewardEntitlementRepository(),
        mock(BillingCheckoutRepository.class),
        mock(BillingEventRepository.class),
        new PlanCatalogService(),
        mock(MercadoPagoBillingClient.class),
        new BillingProperties(),
        new ObjectMapper());
  }

  private static final class InMemoryAiUsageLedgerRepository implements AiUsageLedgerRepository {
    private final Map<String, AiUsageLedger> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public AiUsageLedger save(AiUsageLedger item) {
      var saved =
          item.id() == null
              ? new AiUsageLedger(
                  nextId++,
                  item.userId(),
                  item.resource(),
                  item.usageDate(),
                  item.baseUsed(),
                  item.rewardUsed(),
                  item.rewardGranted(),
                  item.createdAt(),
                  item.updatedAt())
              : item;
      items.put(key(saved.userId(), saved.resource(), saved.usageDate()), saved);
      return saved;
    }

    @Override
    public AiUsageLedger findByUserIdAndResourceAndUsageDate(
        String userId, String resource, LocalDate usageDate) {
      return items.get(key(userId, resource, usageDate));
    }

    private String key(String userId, String resource, LocalDate usageDate) {
      return userId + "|" + resource + "|" + usageDate;
    }
  }

  private static final class InMemoryAdRewardSessionRepository implements AdRewardSessionRepository {
    private final Map<String, AdRewardSession> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public AdRewardSession save(AdRewardSession item) {
      var saved =
          item.id() == null
              ? new AdRewardSession(
                  nextId++,
                  item.publicId(),
                  item.userId(),
                  item.provider(),
                  item.rewardType(),
                  item.status(),
                  item.providerTransactionId(),
                  item.expiresAt(),
                  item.grantedAt(),
                  item.createdAt(),
                  item.updatedAt())
              : item;
      items.put(saved.publicId(), saved);
      return saved;
    }

    @Override
    public AdRewardSession findByPublicId(String publicId) {
      return items.get(publicId);
    }

    @Override
    public boolean existsGrantedByUserIdAndRewardTypeAndProviderTransactionId(
        String userId, String rewardType, String providerTransactionId) {
      return items.values().stream()
          .anyMatch(
              item ->
                  item.userId().equals(userId)
                      && item.rewardType().equals(rewardType)
                      && "GRANTED".equals(item.status())
                      && providerTransactionId.equals(item.providerTransactionId()));
    }
  }

  private static final class InMemoryRewardEntitlementRepository implements RewardEntitlementRepository {
    private final Map<Long, RewardEntitlement> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public RewardEntitlement save(RewardEntitlement item) {
      var saved =
          item.id() == null
              ? new RewardEntitlement(
                  nextId++,
                  item.userId(),
                  item.entitlementType(),
                  item.sourceRewardSessionId(),
                  item.status(),
                  item.startsAt(),
                  item.expiresAt(),
                  item.createdAt(),
                  item.updatedAt())
              : item;
      items.put(saved.id(), saved);
      return saved;
    }

    @Override
    public RewardEntitlement findActive(String userId, String entitlementType, Instant now) {
      return items.values().stream()
          .filter(
              item ->
                  item.userId().equals(userId)
                      && item.entitlementType().equals(entitlementType)
                      && "ACTIVE".equals(item.status())
                      && !item.startsAt().isAfter(now)
                      && item.expiresAt().isAfter(now))
          .findFirst()
          .orElse(null);
    }

    @Override
    public boolean existsStartedBetween(
        String userId, String entitlementType, Instant startsAtInclusive, Instant startsAtExclusive) {
      return items.values().stream()
          .anyMatch(
              item ->
                  item.userId().equals(userId)
                      && item.entitlementType().equals(entitlementType)
                      && !item.startsAt().isBefore(startsAtInclusive)
                      && item.startsAt().isBefore(startsAtExclusive));
    }
  }
}
