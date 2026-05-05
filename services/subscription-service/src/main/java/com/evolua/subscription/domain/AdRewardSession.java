package com.evolua.subscription.domain;

import java.time.Instant;

public record AdRewardSession(
    Long id,
    String publicId,
    String userId,
    String provider,
    String rewardType,
    String status,
    String providerTransactionId,
    Instant expiresAt,
    Instant grantedAt,
    Instant createdAt,
    Instant updatedAt) {}
