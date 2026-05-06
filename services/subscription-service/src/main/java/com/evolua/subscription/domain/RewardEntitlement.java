package com.evolua.subscription.domain;

import java.time.Instant;

public record RewardEntitlement(
    Long id,
    String userId,
    String entitlementType,
    Long sourceRewardSessionId,
    String status,
    Instant startsAt,
    Instant expiresAt,
    Instant createdAt,
    Instant updatedAt) {}
