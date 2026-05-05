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
    String limitMessage,
    String emotionalStateLabel,
    String shortInsight,
    String nextStep,
    String severityLevel,
    java.util.List<String> tags,
    Boolean shouldSuggestAIChat,
    Boolean shouldSuggestHistoryAnalysis,
    CheckInSuggestedTrail suggestedTrailDetail,
    CheckInSuggestedAction suggestedActionDetail) {
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
      Boolean fallbackUsed,
      Boolean quotaLimited,
      Integer quotaRemainingToday,
      Boolean rewardedAdAvailable,
      Boolean upgradeRecommended,
      String limitMessage) {
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
        quotaLimited,
        quotaRemainingToday,
        rewardedAdAvailable,
        upgradeRecommended,
        limitMessage,
        null,
        null,
        null,
        null,
        java.util.List.of(),
        false,
        false,
        null,
        null);
  }

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

  static CheckInAiInsight fromDecision(CheckInDecision decision) {
    return new CheckInAiInsight(
        decision.shortInsight(),
        decision.nextStep(),
        toRiskLevel(decision.severityLevel()),
        null,
        decision.suggestedTrail() == null ? null : decision.suggestedTrail().title(),
        decision.suggestedTrail() == null
            ? ""
            : "Trilha sugerida para apoiar o proximo passo do seu check-in.",
        null,
        null,
        null,
        false,
        false,
        null,
        false,
        false,
        null,
        decision.emotionalStateLabel(),
        decision.shortInsight(),
        decision.nextStep(),
        decision.severityLevel(),
        decision.tags(),
        decision.shouldSuggestAIChat(),
        decision.shouldSuggestHistoryAnalysis(),
        decision.suggestedTrail(),
        decision.suggestedAction());
  }

  private static String toRiskLevel(String severityLevel) {
    return switch (severityLevel == null ? "" : severityLevel) {
      case "critical", "high" -> "high";
      case "medium" -> "medium";
      default -> "low";
    };
  }
}
