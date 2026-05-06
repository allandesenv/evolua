package com.evolua.subscription.interfaces.rest;

import java.time.Instant;

public record SubscriptionAccessResponse(
    String userId,
    Boolean premium,
    String status,
    String planCode,
    Boolean adsEnabled,
    Integer aiQuotaRemainingToday,
    Boolean mentorPremiumPassActive,
    Instant mentorPremiumPassEndsAt,
    Boolean mentorRewardedAdAvailable) {}
