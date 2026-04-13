package com.evolua.emotional.application;

import org.springframework.stereotype.Service;

@Service
public class CheckInInsightOrchestrator {
  private final CheckInService checkInService;
  private final CheckInInsightClient checkInInsightClient;
  private final JourneyTrailClient journeyTrailClient;

  public CheckInInsightOrchestrator(
      CheckInService checkInService,
      CheckInInsightClient checkInInsightClient,
      JourneyTrailClient journeyTrailClient) {
    this.checkInService = checkInService;
    this.checkInInsightClient = checkInInsightClient;
    this.journeyTrailClient = journeyTrailClient;
  }

  public CheckInCreationResult createWithInsight(
      String authorizationHeader,
      String userId,
      String mood,
      String reflection,
      Integer energyLevel) {
    var fallbackPractice = fallbackPractice(mood, energyLevel);
    var created =
        checkInService.create(userId, mood, reflection, energyLevel, fallbackPractice);
    var aiInsight = checkInInsightClient.generateInsight(authorizationHeader, created);
    var journeyTrail =
        journeyTrailClient.upsertCurrentJourney(
            authorizationHeader, aiInsight.generatedTrailDraft(), aiInsight.journeyPlan());
    var enrichedInsight =
        journeyTrail == null
            ? aiInsight
            : new CheckInAiInsight(
                aiInsight.insight(),
                aiInsight.suggestedAction(),
                aiInsight.riskLevel(),
                journeyTrail.id(),
                journeyTrail.title(),
                aiInsight.suggestedTrailReason(),
                aiInsight.suggestedSpace(),
                aiInsight.journeyPlan(),
                aiInsight.generatedTrailDraft(),
                aiInsight.fallbackUsed());
    var finalPractice =
        enrichedInsight.suggestedAction() == null || enrichedInsight.suggestedAction().isBlank()
            ? fallbackPractice
            : enrichedInsight.suggestedAction();
    var updatedCheckIn =
        finalPractice.equals(created.recommendedPractice())
            ? created
            : checkInService.updateRecommendedPractice(created, finalPractice);
    return new CheckInCreationResult(updatedCheckIn, enrichedInsight);
  }

  private String fallbackPractice(String mood, Integer energyLevel) {
    var normalizedMood = mood == null ? "" : mood.toLowerCase();
    if (energyLevel != null && energyLevel <= 4) {
      return "Escolha uma pausa curta de regulacao por 5 minutos.";
    }
    if (normalizedMood.contains("ans")) {
      return "Comece por respiracao guiada ou uma pausa sensorial curta.";
    }
    if (normalizedMood.contains("cans")) {
      return "Va para uma pratica leve de recuperacao e desaceleracao.";
    }
    return "Siga com uma unica acao leve para sustentar o resto do dia.";
  }
}
