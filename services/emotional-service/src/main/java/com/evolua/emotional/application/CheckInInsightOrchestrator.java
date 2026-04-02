package com.evolua.emotional.application;

import org.springframework.stereotype.Service;

@Service
public class CheckInInsightOrchestrator {
  private final CheckInService checkInService;
  private final CheckInInsightClient checkInInsightClient;

  public CheckInInsightOrchestrator(
      CheckInService checkInService, CheckInInsightClient checkInInsightClient) {
    this.checkInService = checkInService;
    this.checkInInsightClient = checkInInsightClient;
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
    var finalPractice =
        aiInsight.suggestedAction() == null || aiInsight.suggestedAction().isBlank()
            ? fallbackPractice
            : aiInsight.suggestedAction();
    var updatedCheckIn =
        finalPractice.equals(created.recommendedPractice())
            ? created
            : checkInService.updateRecommendedPractice(created, finalPractice);
    return new CheckInCreationResult(updatedCheckIn, aiInsight);
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
