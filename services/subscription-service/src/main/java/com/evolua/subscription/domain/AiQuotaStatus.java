package com.evolua.subscription.domain;

public record AiQuotaStatus(
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
    String limitMessage) {}
