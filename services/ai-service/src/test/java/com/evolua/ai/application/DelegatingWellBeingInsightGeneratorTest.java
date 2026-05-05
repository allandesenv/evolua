package com.evolua.ai.application;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.evolua.ai.config.AiProperties;
import com.evolua.ai.infrastructure.security.AuthenticatedUser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DelegatingWellBeingInsightGeneratorTest {
  private static final AuthenticatedUser USER =
      new AuthenticatedUser("user-123", "user@evolua.app", List.of("ROLE_USER"));

  @Test
  void usesOpenAiGeneratorWhenProviderIsOpenAi() {
    var properties = new AiProperties();
    properties.setProvider("openai");
    var heuristic = Mockito.mock(RuleBasedWellBeingInsightGenerator.class);
    var openAi = Mockito.mock(OpenAiWellBeingInsightGenerator.class);
    var delegating = new DelegatingWellBeingInsightGenerator(properties, heuristic, openAi);
    var expected = new CheckInInsight("insight", "action", "low", 1L, "Trilha", "reason", null, null, null, false);

    when(openAi.generate(Mockito.any(), Mockito.any(), Mockito.anyList(), Mockito.anyList(), Mockito.any()))
        .thenReturn(expected);

    var result =
        delegating.generate(
            new CurrentCheckInInput("calmo", "", 7, null),
            new EmotionalContextSnapshot(List.of(), 7, "calmo", "estavel"),
            List.of(),
            List.of(),
            USER);

    assertSame(expected, result);
    verifyNoInteractions(heuristic);
  }

  @Test
  void usesHeuristicGeneratorWhenProviderIsHeuristic() {
    var properties = new AiProperties();
    properties.setProvider("heuristic");
    var heuristic = Mockito.mock(RuleBasedWellBeingInsightGenerator.class);
    var openAi = Mockito.mock(OpenAiWellBeingInsightGenerator.class);
    var delegating = new DelegatingWellBeingInsightGenerator(properties, heuristic, openAi);
    var expected = new CheckInInsight("insight", "action", "low", null, null, "reason", null, null, null, false);

    when(heuristic.generate(Mockito.any(), Mockito.any(), Mockito.anyList(), Mockito.anyList(), Mockito.any()))
        .thenReturn(expected);

    var result =
        delegating.generate(
            new CurrentCheckInInput("calmo", "", 7, null),
            new EmotionalContextSnapshot(List.of(), 7, "calmo", "estavel"),
            List.of(),
            List.of(),
            USER);

    assertSame(expected, result);
    verifyNoInteractions(openAi);
  }
}
