package com.evolua.emotional.interfaces.rest;

import java.util.List;

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
    String limitMessage,
    String emotionalStateLabel,
    String shortInsight,
    String nextStep,
    String severityLevel,
    List<String> tags,
    Boolean shouldSuggestAIChat,
    Boolean shouldSuggestHistoryAnalysis,
    CheckInSuggestedTrailResponse suggestedTrailDetail,
    CheckInSuggestedActionResponse suggestedActionDetail) {}
