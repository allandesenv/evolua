package com.evolua.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RuleBasedWellBeingInsightGeneratorTest {
  private final RuleBasedWellBeingInsightGenerator generator =
      new RuleBasedWellBeingInsightGenerator(new CuratedJourneyLinkLibrary());

  @Test
  void generatedTrailDraftContentInterpolatesRealValues() {
    var result =
        generator.generate(
            new CurrentCheckInInput("ansioso", "Estou com a mente acelerada por causa do trabalho.", 4, null),
            new EmotionalContextSnapshot(List.of(), 5, "ansioso", "mais fragil"),
            List.of(),
            List.of(),
            List.of("ROLE_USER"));

    assertNotNull(result.generatedTrailDraft());
    assertTrue(result.generatedTrailDraft().content().contains("# Voltar ao eixo em dias de ansiedade"));
    assertTrue(result.generatedTrailDraft().content().contains("Uma trilha privada para reduzir ativacao"));
    assertTrue(result.generatedTrailDraft().content().contains("a tendencia aparece como mais fragil"));
    assertFalse(result.generatedTrailDraft().content().contains("$title"));
    assertFalse(result.generatedTrailDraft().content().contains("$summary"));
    assertFalse(result.generatedTrailDraft().content().contains("${"));
  }

  @Test
  void productivityFallbackDoesNotEchoReflectionOrInventFragility() {
    var reflection = "Consegui resolver tarefas importantes e me senti produtivo";

    var result =
        generator.generate(
            new CurrentCheckInInput("produtivo", reflection, 7, null),
            new EmotionalContextSnapshot(List.of(), 4, "ansioso", "mais fragil"),
            List.of(),
            List.of(),
            List.of("ROLE_USER"));

    assertEquals("low", result.riskLevel());
    assertTrue(result.insight().contains("momento de avanco"));
    assertTrue(result.suggestedAction().contains("Feche o ciclo"));
    assertFalse(result.insight().contains(reflection));
    assertFalse(result.suggestedAction().contains(reflection));
    assertFalse(result.insight().contains("\""));
    assertFalse(result.insight().contains("ritmo mais fragil"));
  }

  @Test
  void anxietyFallbackSuggestsRegulationAndSmallStep() {
    var reflection = "Minha mente esta acelerada com muitas demandas";

    var result =
        generator.generate(
            new CurrentCheckInInput("ansioso", reflection, 5, null),
            new EmotionalContextSnapshot(List.of(), 6, "calmo", "estavel"),
            List.of(),
            List.of(),
            List.of("ROLE_USER"));

    assertEquals("medium", result.riskLevel());
    assertTrue(result.insight().contains("tensao relevante"));
    assertTrue(result.suggestedAction().contains("pausa de 3 minutos"));
    assertFalse(result.insight().contains(reflection));
    assertFalse(result.suggestedAction().contains(reflection));
  }

  @Test
  void fatigueFallbackPrioritizesRecoveryInsteadOfProductivity() {
    var reflection = "Dormi mal e estou sem energia";

    var result =
        generator.generate(
            new CurrentCheckInInput("cansado", reflection, 3, null),
            new EmotionalContextSnapshot(List.of(), 7, "produtivo", "estavel"),
            List.of(),
            List.of(),
            List.of("ROLE_USER"));

    assertEquals("low", result.riskLevel());
    assertTrue(result.insight().contains("desgaste"));
    assertTrue(result.suggestedAction().contains("modo leve"));
    assertFalse(result.insight().contains(reflection));
    assertFalse(result.suggestedAction().toLowerCase().contains("produtiv"));
  }

  @Test
  void fallbackWithoutReflectionDoesNotInventAUserStory() {
    var result =
        generator.generate(
            new CurrentCheckInInput("calmo", "", 7, null),
            new EmotionalContextSnapshot(List.of(), 7, "calmo", "estavel"),
            List.of(),
            List.of(),
            List.of("ROLE_USER"));

    assertEquals("low", result.riskLevel());
    assertTrue(result.insight().contains("base favoravel"));
    assertFalse(result.insight().contains("trecho"));
    assertFalse(result.insight().contains("\""));
    assertFalse(result.insight().contains("relato"));
  }

  @Test
  void highRiskStillPrioritizesSafety() {
    var result =
        generator.generate(
            new CurrentCheckInInput("desesperado", "Penso em me machucar hoje", 1, null),
            new EmotionalContextSnapshot(List.of(), 4, "triste", "mais fragil"),
            List.of(),
            List.of(),
            List.of("ROLE_USER"));

    assertEquals("high", result.riskLevel());
    assertTrue(result.insight().contains("apoio humano"));
    assertTrue(result.suggestedAction().contains("alguem de confianca"));
    assertTrue(result.suggestedTrailReason().contains("seguranca"));
    assertFalse(result.insight().contains("Penso em me machucar hoje"));
  }
}
