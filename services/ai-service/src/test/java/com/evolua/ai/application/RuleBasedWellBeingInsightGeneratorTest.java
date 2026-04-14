package com.evolua.ai.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RuleBasedWellBeingInsightGeneratorTest {
  @Test
  void generatedTrailDraftContentInterpolatesRealValues() {
    var generator = new RuleBasedWellBeingInsightGenerator(new CuratedJourneyLinkLibrary());

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
}
