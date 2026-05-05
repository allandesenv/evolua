package com.evolua.ai.application;

public record JourneyChatResponse(
    String reply,
    String riskLevel,
    String suggestedNextStep,
    Boolean fallbackUsed,
    Boolean quotaLimited,
    Integer quotaRemainingToday,
    Boolean rewardedAdAvailable,
    Boolean upgradeRecommended,
    String limitMessage) {
  public JourneyChatResponse(String reply, String riskLevel, String suggestedNextStep, Boolean fallbackUsed) {
    this(reply, riskLevel, suggestedNextStep, fallbackUsed, false, null, false, false, null);
  }

  public JourneyChatResponse withQuotaMetadata(AiQuotaDecision quota, boolean quotaLimited) {
    return new JourneyChatResponse(
        reply,
        riskLevel,
        suggestedNextStep,
        fallbackUsed,
        quotaLimited,
        quota == null ? quotaRemainingToday : quota.remainingToday(),
        quota == null ? rewardedAdAvailable : quota.rewardedAdAvailable(),
        quota == null ? upgradeRecommended : quota.upgradeRecommended(),
        quota == null ? limitMessage : quota.limitMessage());
  }
}
