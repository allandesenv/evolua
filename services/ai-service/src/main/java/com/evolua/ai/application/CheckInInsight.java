package com.evolua.ai.application;

public record CheckInInsight(
    String insight,
    String suggestedAction,
    String riskLevel,
    Long suggestedTrailId,
    String suggestedTrailTitle,
    String suggestedTrailReason,
    SuggestedSpace suggestedSpace,
    JourneyPlan journeyPlan,
    GeneratedTrailDraft generatedTrailDraft,
    Boolean fallbackUsed,
    Boolean quotaLimited,
    Integer quotaRemainingToday,
    Boolean rewardedAdAvailable,
    Boolean upgradeRecommended,
    String limitMessage) {
  public CheckInInsight(
      String insight,
      String suggestedAction,
      String riskLevel,
      Long suggestedTrailId,
      String suggestedTrailTitle,
      String suggestedTrailReason,
      SuggestedSpace suggestedSpace,
      JourneyPlan journeyPlan,
      GeneratedTrailDraft generatedTrailDraft,
      Boolean fallbackUsed) {
    this(
        insight,
        suggestedAction,
        riskLevel,
        suggestedTrailId,
        suggestedTrailTitle,
        suggestedTrailReason,
        suggestedSpace,
        journeyPlan,
        generatedTrailDraft,
        fallbackUsed,
        false,
        null,
        false,
        false,
        null);
  }

  public CheckInInsight withQuotaMetadata(AiQuotaDecision quota, boolean quotaLimited) {
    return new CheckInInsight(
        insight,
        suggestedAction,
        riskLevel,
        suggestedTrailId,
        suggestedTrailTitle,
        suggestedTrailReason,
        suggestedSpace,
        journeyPlan,
        generatedTrailDraft,
        fallbackUsed,
        quotaLimited,
        quota == null ? quotaRemainingToday : quota.remainingToday(),
        quota == null ? rewardedAdAvailable : quota.rewardedAdAvailable(),
        quota == null ? upgradeRecommended : quota.upgradeRecommended(),
        quota == null ? limitMessage : quota.limitMessage());
  }
}
