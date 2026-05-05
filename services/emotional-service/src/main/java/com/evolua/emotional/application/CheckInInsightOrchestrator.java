package com.evolua.emotional.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CheckInInsightOrchestrator {
  private final CheckInService checkInService;
  private final CheckInInsightClient checkInInsightClient;
  private final JourneyTrailClient journeyTrailClient;
  private final EmotionalCheckinEngine emotionalCheckinEngine;
  private final boolean aiCheckInEnabled;

  public CheckInInsightOrchestrator(
      CheckInService checkInService,
      CheckInInsightClient checkInInsightClient,
      JourneyTrailClient journeyTrailClient,
      EmotionalCheckinEngine emotionalCheckinEngine,
      @Value("${app.ai.checkin-enabled:false}") boolean aiCheckInEnabled) {
    this.checkInService = checkInService;
    this.checkInInsightClient = checkInInsightClient;
    this.journeyTrailClient = journeyTrailClient;
    this.emotionalCheckinEngine = emotionalCheckinEngine;
    this.aiCheckInEnabled = aiCheckInEnabled;
  }

  public CheckInCreationResult createWithInsight(
      String authorizationHeader,
      String userId,
      String mood,
      String reflection,
      Integer energyLevel,
      String emotion,
      Integer intensity,
      String energy,
      String context,
      String note) {
    var normalizedEmotion = firstText(emotion, mood, "confuso");
    var normalizedNote = firstText(note, reflection, "");
    var normalizedEnergy = CheckinRuleEvaluator.normalizeEnergy(energy, energyLevel);
    var normalizedIntensity = intensity == null ? energyLevel : intensity;
    var recentHistory = checkInService.recentSince(userId, Instant.now().minus(7, ChronoUnit.DAYS));
    var decision =
        emotionalCheckinEngine.decide(
            new CheckInDecisionInput(
                userId,
                normalizedEmotion,
                normalizedIntensity,
                normalizedEnergy,
                context,
                normalizedNote,
                energyLevel),
            recentHistory);
    var deterministicInsight = CheckInAiInsight.fromDecision(decision);
    if (!aiCheckInEnabled) {
      var created =
          checkInService.create(
              userId,
              firstText(mood, normalizedEmotion, "confuso"),
              normalizedNote,
              energyLevel,
              decision.nextStep(),
              normalizedEmotion,
              normalizedIntensity,
              normalizedEnergy,
              context,
              String.join(",", decision.tags()),
              decision.severityLevel());
      return new CheckInCreationResult(created, deterministicInsight);
    }

    var fallbackPractice = fallbackPractice(mood, energyLevel);
    var created =
        checkInService.create(
            userId,
            firstText(mood, normalizedEmotion, "confuso"),
            normalizedNote,
            energyLevel,
            fallbackPractice,
            normalizedEmotion,
            normalizedIntensity,
            normalizedEnergy,
            context,
            String.join(",", decision.tags()),
            decision.severityLevel());
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
                aiInsight.fallbackUsed(),
                aiInsight.quotaLimited(),
                aiInsight.quotaRemainingToday(),
                aiInsight.rewardedAdAvailable(),
                aiInsight.upgradeRecommended(),
                aiInsight.limitMessage());
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

  public CheckInCreationResult createWithInsight(
      String authorizationHeader,
      String userId,
      String mood,
      String reflection,
      Integer energyLevel) {
    return createWithInsight(
        authorizationHeader, userId, mood, reflection, energyLevel, mood, null, null, null, reflection);
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

  private String firstText(String primary, String secondary, String fallback) {
    if (primary != null && !primary.isBlank()) {
      return primary.trim();
    }
    if (secondary != null && !secondary.isBlank()) {
      return secondary.trim();
    }
    return fallback;
  }
}
