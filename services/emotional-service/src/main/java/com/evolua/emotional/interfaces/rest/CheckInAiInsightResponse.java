package com.evolua.emotional.interfaces.rest;

public record CheckInAiInsightResponse(
    String insight,
    String suggestedAction,
    String riskLevel,
    Long suggestedTrailId,
    String suggestedTrailTitle,
    String suggestedTrailReason,
    CheckInAiSuggestedSpaceResponse suggestedSpace,
    CheckInAiJourneyPlanResponse journeyPlan,
    CheckInAiGeneratedTrailDraftResponse generatedTrailDraft,
    Boolean fallbackUsed,
    Boolean quotaLimited,
    Integer quotaRemainingToday,
    Boolean rewardedAdAvailable,
    Boolean upgradeRecommended,
    String limitMessage) {}
