package com.evolua.emotional.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolua.emotional.domain.CheckIn;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmotionalCheckinEngineTest {
  private final EmotionalCheckinEngine engine =
      new EmotionalCheckinEngine(new CheckinRuleEvaluator(new EmotionalStateCatalog(), true, true));

  @Test
  void highAnxietyWithHighEnergyReturnsAcceleratedMindAndSuggestsChat() {
    var decision = decide("ansioso", 8, "alta", List.of());

    assertEquals("mente acelerada", decision.emotionalStateLabel());
    assertEquals("Desacelerar e organizar", decision.suggestedTrail().title());
    assertTrue(decision.shouldSuggestAIChat());
  }

  @Test
  void highSadnessWithLowEnergyReturnsCareAndSuggestsChat() {
    var decision = decide("triste", 8, "baixa", List.of());

    assertEquals("recolhimento emocional", decision.emotionalStateLabel());
    assertEquals("Acolhimento e presença", decision.suggestedTrail().title());
    assertTrue(decision.shouldSuggestAIChat());
  }

  @Test
  void highIrritationWithHighEnergyReturnsPauseWithoutChat() {
    var decision = decide("irritado", 7, "alta", List.of());

    assertEquals("tensão ativa", decision.emotionalStateLabel());
    assertEquals("Pausa antes da reação", decision.suggestedTrail().title());
    assertFalse(decision.shouldSuggestAIChat());
  }

  @Test
  void fatigueWithLowEnergyReturnsRecovery() {
    var decision = decide("cansado", 5, "baixa", List.of());

    assertEquals("baixa energia", decision.emotionalStateLabel());
    assertEquals("Recuperar energia", decision.suggestedTrail().title());
  }

  @Test
  void positiveStatesReturnGoodStateReinforcement() {
    for (var emotion : List.of("feliz", "motivado", "grato")) {
      var decision = decide(emotion, 6, "média", List.of());
      assertEquals("estado expansivo", decision.emotionalStateLabel());
      assertEquals("Fortalecer bons estados", decision.suggestedTrail().title());
      assertFalse(decision.shouldSuggestAIChat());
    }
  }

  @Test
  void repeatedAnxietyInSevenDaysSuggestsHistoryAnalysis() {
    var history =
        List.of(
            history("ansioso", 8),
            history("ansioso", 7));

    var decision = decide("ansioso", 8, "alta", history);

    assertTrue(decision.shouldSuggestHistoryAnalysis());
    assertEquals("Desacelerar e organizar", decision.suggestedTrail().title());
  }

  private CheckInDecision decide(
      String emotion, Integer intensity, String energy, List<CheckIn> history) {
    return engine.decide(
        new CheckInDecisionInput("user-1", emotion, intensity, energy, "trabalho", null, null),
        history);
  }

  private CheckIn history(String emotion, Integer intensity) {
    return new CheckIn(
        1L,
        "user-1",
        emotion,
        "",
        6,
        "practice",
        Instant.now(),
        emotion,
        intensity,
        "alta",
        "trabalho",
        null,
        null);
  }
}
