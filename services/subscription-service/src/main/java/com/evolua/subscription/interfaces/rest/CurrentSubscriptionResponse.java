package com.evolua.subscription.interfaces.rest;

import java.time.Instant;

public record CurrentSubscriptionResponse(
    Long id,
    String userId,
    String planCode,
    String status,
    String billingCycle,
    Boolean premium,
    String provider,
    Boolean adsEnabled,
    Integer aiQuotaRemainingToday,
    Instant currentPeriodEndsAt,
    Instant canceledAt,
    Instant createdAt,
    Instant updatedAt) {}
