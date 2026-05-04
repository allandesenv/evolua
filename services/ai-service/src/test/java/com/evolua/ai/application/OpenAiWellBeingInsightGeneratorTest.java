package com.evolua.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolua.ai.config.AiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpenAiWellBeingInsightGeneratorTest {
  @Test
  void missingOpenAiConfigReturnsSafeFallbackMarkedAsFallback() {
    var properties = new AiProperties();
    properties.setProvider("openai");
    properties.setModel("");
    properties.setApiKey("");

    var generator =
        new OpenAiWellBeingInsightGenerator(
            properties,
            new RuleBasedWellBeingInsightGenerator(new CuratedJourneyLinkLibrary()),
            new CuratedJourneyLinkLibrary(),
            new ObjectMapper());

    var result =
        generator.generate(
            new CurrentCheckInInput(
                "produtivo", "Consegui resolver tarefas importantes e me senti produtivo", 7, null),
            new EmotionalContextSnapshot(List.of(), 7, "produtivo", "estavel"),
            List.of(),
            List.of(),
            List.of("ROLE_USER"));

    assertEquals(Boolean.TRUE, result.fallbackUsed());
    assertTrue(result.insight().contains("momento de avanco"));
    assertTrue(result.suggestedAction().contains("Feche o ciclo"));
  }
}
