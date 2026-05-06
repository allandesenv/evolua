package com.evolua.subscription.domain;

import java.time.Instant;

public interface RewardEntitlementRepository {
  RewardEntitlement save(RewardEntitlement item);

  RewardEntitlement findActive(String userId, String entitlementType, Instant now);

  boolean existsStartedBetween(
      String userId, String entitlementType, Instant startsAtInclusive, Instant startsAtExclusive);
}
