package com.evolua.emotional.application;

public record CheckInAiInsight(
    String insight,
    String suggestedAction,
    String riskLevel,
    Long suggestedTrailId,
    String suggestedTrailTitle,
    String suggestedTrailReason,
    CheckInAiSuggestedSpace suggestedSpace,
    CheckInAiJourneyPlan journeyPlan,
    CheckInAiGeneratedTrailDraft generatedTrailDraft,
    Boolean fallbackUsed,
    Boolean quotaLimited,
    Integer quotaRemainingToday,
    Boolean rewardedAdAvailable,
    Boolean upgradeRecommended,
    String limitMessage) {
  public CheckInAiInsight(
      String insight,
      String suggestedAction,
      String riskLevel,
      Long suggestedTrailId,
      String suggestedTrailTitle,
      String suggestedTrailReason,
      CheckInAiSuggestedSpace suggestedSpace,
      CheckInAiJourneyPlan journeyPlan,
      CheckInAiGeneratedTrailDraft generatedTrailDraft,
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
}
