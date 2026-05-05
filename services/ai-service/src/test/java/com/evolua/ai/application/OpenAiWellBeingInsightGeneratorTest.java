package com.evolua.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.evolua.ai.config.AiProperties;
import com.evolua.ai.infrastructure.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
            Mockito.mock(SubscriptionQuotaClient.class),
            new CuratedJourneyLinkLibrary(),
            new ObjectMapper());

    var result =
        generator.generate(
            new CurrentCheckInInput(
                "produtivo", "Consegui resolver tarefas importantes e me senti produtivo", 7, null),
            new EmotionalContextSnapshot(List.of(), 7, "produtivo", "estavel"),
            List.of(),
            List.of(),
            new AuthenticatedUser("user-123", "user@evolua.app", List.of("ROLE_USER")));

    assertEquals(Boolean.TRUE, result.fallbackUsed());
    assertTrue(result.insight().contains("momento de avanco"));
    assertTrue(result.suggestedAction().contains("Feche o ciclo"));
  }

  @Test
  void exhaustedQuotaReturnsRuleBasedFallbackWithoutExternalAi() {
    var properties = new AiProperties();
    properties.setProvider("openai");
    properties.setModel("test-model");
    properties.setApiKey("test-key");
    properties.setBaseUrl("http://127.0.0.1:1");
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    when(quotaClient.consume("user-123"))
        .thenReturn(
            new AiQuotaDecision(
                false,
                false,
                0,
                true,
                true,
                "Seu limite gratuito de IA acabou por hoje."));
    var generator =
        new OpenAiWellBeingInsightGenerator(
            properties,
            new RuleBasedWellBeingInsightGenerator(new CuratedJourneyLinkLibrary()),
            quotaClient,
            new CuratedJourneyLinkLibrary(),
            new ObjectMapper());

    var result =
        generator.generate(
            new CurrentCheckInInput("ansioso", "Estou acelerado por causa do trabalho.", 4, null),
            new EmotionalContextSnapshot(List.of(), 4, "ansioso", "mais fragil"),
            List.of(),
            List.of(),
            new AuthenticatedUser("user-123", "user@evolua.app", List.of("ROLE_USER")));

    assertEquals(Boolean.TRUE, result.fallbackUsed());
    assertEquals(Boolean.TRUE, result.quotaLimited());
    assertEquals(Boolean.TRUE, result.rewardedAdAvailable());
    assertTrue(result.limitMessage().contains("limite gratuito"));
  }

  @Test
  void highRiskCheckInDoesNotConsumeQuota() {
    var properties = new AiProperties();
    properties.setProvider("openai");
    properties.setModel("test-model");
    properties.setApiKey("test-key");
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    var generator =
        new OpenAiWellBeingInsightGenerator(
            properties,
            new RuleBasedWellBeingInsightGenerator(new CuratedJourneyLinkLibrary()),
            quotaClient,
            new CuratedJourneyLinkLibrary(),
            new ObjectMapper());

    var result =
        generator.generate(
            new CurrentCheckInInput("desesperado", "Penso em me machucar hoje.", 1, null),
            new EmotionalContextSnapshot(List.of(), 1, "desesperado", "critico"),
            List.of(),
            List.of(),
            new AuthenticatedUser("user-123", "user@evolua.app", List.of("ROLE_USER")));

    assertEquals("high", result.riskLevel());
    verifyNoInteractions(quotaClient);
  }
}
