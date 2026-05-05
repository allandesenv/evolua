package com.evolua.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.evolua.ai.config.AiProperties;
import com.evolua.ai.infrastructure.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JourneyChatServiceTest {
  private static final AuthenticatedUser USER =
      new AuthenticatedUser("user-123", "user@evolua.app", List.of("ROLE_USER"));

  @Test
  void promptContextIncludesRecentCheckInsAndJourneySections() {
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    var service =
        new JourneyChatService(new AiProperties(), contentClient, emotionalClient, quotaClient, new ObjectMapper());

    var journey =
        new JourneyTrailSnapshot(
            9L,
            "Voltar ao eixo em dias de ansiedade",
            "Resumo da jornada",
            """
            # Voltar ao eixo em dias de ansiedade

            ## Leitura do momento
            Seu corpo esta em alerta.

            ## Direcao da jornada
            Primeiro reduzir a pressa, depois organizar o foco.

            ## Exercicios
            1. Respirar

            ## Plano de 24 horas
            Fazer uma pausa curta.

            ## Plano de 7 dias
            Repetir o check-in.
            """,
            "ansiedade",
            "neuro-stoic-contemplative",
            List.of());

    var emotionalContext =
        new EmotionalContextSnapshot(
            List.of(
                new RecentCheckInSnapshot(
                    11L,
                    "ansioso",
                    "Fiquei sobrecarregado por reunioes e notificacoes.",
                    4,
                    "Respirar por 3 minutos",
                    Instant.parse("2026-04-13T12:00:00Z"))),
            4,
            "ansioso",
            "mais fragil");

    var payload =
        service.buildPromptContext(
            "Hoje ainda estou muito acelerado.",
            List.of(new JourneyChatMessage("user", "Ontem eu tambem estava no limite.")),
            journey,
            emotionalContext,
            9L);

    assertEquals("Hoje ainda estou muito acelerado.", payload.get("mensagemUsuario"));
    assertNotNull(payload.get("contextoEmocionalRecente"));
    assertNotNull(payload.get("jornadaAtual"));

    @SuppressWarnings("unchecked")
    var contextPayload = (Map<String, Object>) payload.get("contextoEmocionalRecente");
    @SuppressWarnings("unchecked")
    var lastCheckIn = (Map<String, Object>) contextPayload.get("ultimoCheckIn");
    @SuppressWarnings("unchecked")
    var pattern = (Map<String, Object>) contextPayload.get("padraoRecente");
    @SuppressWarnings("unchecked")
    var journeyPayload = (Map<String, Object>) payload.get("jornadaAtual");
    @SuppressWarnings("unchecked")
    var sections = (Map<String, String>) journeyPayload.get("secoesEssenciais");

    assertEquals("Fiquei sobrecarregado por reunioes e notificacoes.", lastCheckIn.get("reflection"));
    assertEquals(4, pattern.get("averageEnergy"));
    assertEquals("mais fragil", pattern.get("energyTrendLabel"));
    assertTrue(sections.get("Direcao da jornada").contains("reduzir a pressa"));
    assertTrue(sections.get("Plano de 24 horas").contains("pausa curta"));
  }

  @Test
  void fallbackReplyUsesRecentReflectionAndJourneyDirection() {
    var properties = new AiProperties();
    properties.setApiKey("");
    properties.setModel("");
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    var service =
        new JourneyChatService(properties, contentClient, emotionalClient, quotaClient, new ObjectMapper());

    when(contentClient.fetchCurrentJourney("Bearer token"))
        .thenReturn(
            new JourneyTrailSnapshot(
                7L,
                "Recuperacao gentil para corpo e mente",
                "Resumo",
                """
                ## Direcao da jornada
                Reduzir a carga e voltar ao corpo antes de reorganizar o dia.
                """,
                "recuperacao",
                "neuro-biblical-restorative",
                List.of()));
    when(emotionalClient.fetchRecentContext("Bearer token"))
        .thenReturn(
            new EmotionalContextSnapshot(
                List.of(
                    new RecentCheckInSnapshot(
                        1L,
                        "cansado",
                        "Dormi pouco e fiquei esgotado depois de dois dias intensos.",
                        3,
                        "Pausa curta",
                        Instant.parse("2026-04-13T09:00:00Z"))),
                3,
                "cansado",
                "mais fragil"));

    var result =
        service.reply(
            "Bearer token",
            USER,
            "Ainda estou travado e sem clareza para comecar.",
            List.of(),
            7L);

    assertTrue(result.fallbackUsed());
    assertFalse(result.reply().startsWith("Voce trouxe"));
    assertTrue(result.reply().contains("Reduzir a carga e voltar ao corpo"));
    assertNotEquals("Escolha um exercicio da jornada e pratique por 10 minutos.", result.suggestedNextStep());
  }

  @Test
  void fallbackReplyAnswersMeditationRequestWithConcretePractice() {
    var properties = new AiProperties();
    properties.setApiKey("");
    properties.setModel("");
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    var service =
        new JourneyChatService(properties, contentClient, emotionalClient, quotaClient, new ObjectMapper());

    when(contentClient.fetchCurrentJourney("Bearer token")).thenReturn(null);
    when(emotionalClient.fetchRecentContext("Bearer token")).thenReturn(null);

    var result =
        service.reply(
            "Bearer token",
            USER,
            "Qual a melhor meditacao pra mim agora?",
            List.of(),
            null);

    assertTrue(result.fallbackUsed());
    assertFalse(result.reply().startsWith("Voce trouxe"));
    assertTrue(result.reply().contains("3 minutos"));
    assertTrue(result.reply().contains("pontos de contato"));
    assertTrue(result.suggestedNextStep().contains("respiracao"));
  }

  @Test
  void fallbackReplyDoesNotPushJourneyExerciseWhenUserOnlyWantsConversation() {
    var properties = new AiProperties();
    properties.setApiKey("");
    properties.setModel("");
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    var service =
        new JourneyChatService(properties, contentClient, emotionalClient, quotaClient, new ObjectMapper());

    when(contentClient.fetchCurrentJourney("Bearer token")).thenReturn(null);
    when(emotionalClient.fetchRecentContext("Bearer token")).thenReturn(null);

    var result =
        service.reply(
            "Bearer token",
            USER,
            "Ainda nao iniciei uma jornada, estou apenas batendo um papo mesmo",
            List.of(),
            null);

    assertTrue(result.fallbackUsed());
    assertFalse(result.reply().startsWith("Voce trouxe"));
    assertTrue(result.reply().contains("Pode ser so conversa"));
    assertFalse(result.suggestedNextStep().contains("exercicio da jornada"));
  }

  @Test
  void fallbackReplyVariesByMessageIntent() {
    var properties = new AiProperties();
    properties.setApiKey("");
    properties.setModel("");
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    var service =
        new JourneyChatService(properties, contentClient, emotionalClient, quotaClient, new ObjectMapper());

    when(contentClient.fetchCurrentJourney("Bearer token")).thenReturn(null);
    when(emotionalClient.fetchRecentContext("Bearer token")).thenReturn(null);

    var sadResult =
        service.reply(
            "Bearer token",
            USER,
            "Estou triste e com pensamentos intrusivos hoje",
            List.of(),
            null);
    var meditationResult =
        service.reply(
            "Bearer token",
            USER,
            "Qual a melhor meditacao pra mim agora?",
            List.of(),
            null);

    assertNotEquals(sadResult.reply(), meditationResult.reply());
    assertNotEquals(sadResult.suggestedNextStep(), meditationResult.suggestedNextStep());
  }

  @Test
  void externalAiFailureFallsBackToSafeReply() {
    var properties = new AiProperties();
    properties.setApiKey("test-key");
    properties.setModel("test-model");
    properties.setBaseUrl("http://127.0.0.1:1");
    properties.setTimeoutSeconds(1);
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    when(quotaClient.consume("user-123"))
        .thenReturn(new AiQuotaDecision(true, false, 0, false, true, null));
    var service =
        new JourneyChatService(properties, contentClient, emotionalClient, quotaClient, new ObjectMapper());

    when(contentClient.fetchCurrentJourney("Bearer token")).thenReturn(null);
    when(emotionalClient.fetchRecentContext("Bearer token")).thenReturn(null);

    var result =
        service.reply(
            "Bearer token",
            USER,
            "Estou triste e com pensamentos intrusivos hoje",
            List.of(),
            null);

    assertTrue(result.fallbackUsed());
    assertFalse(result.reply().startsWith("Voce trouxe"));
    assertTrue(result.reply().contains("pensamentos intrusivos"));
  }

  @Test
  void highRiskReplyDoesNotConsumeQuota() {
    var properties = new AiProperties();
    properties.setApiKey("test-key");
    properties.setModel("test-model");
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    var service =
        new JourneyChatService(properties, contentClient, emotionalClient, quotaClient, new ObjectMapper());

    var result =
        service.reply(
            "Bearer token",
            USER,
            "Estou sem saida e penso em me machucar.",
            List.of(),
            null);

    assertEquals("high", result.riskLevel());
    verifyNoInteractions(quotaClient, contentClient, emotionalClient);
  }

  @Test
  void exhaustedQuotaReturnsFallbackWithLimitMetadata() {
    var properties = new AiProperties();
    properties.setApiKey("test-key");
    properties.setModel("test-model");
    properties.setBaseUrl("http://127.0.0.1:1");
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var quotaClient = Mockito.mock(SubscriptionQuotaClient.class);
    when(contentClient.fetchCurrentJourney("Bearer token")).thenReturn(null);
    when(emotionalClient.fetchRecentContext("Bearer token")).thenReturn(null);
    when(quotaClient.consume("user-123"))
        .thenReturn(
            new AiQuotaDecision(
                false,
                false,
                0,
                true,
                true,
                "Seu limite gratuito de IA acabou por hoje."));
    var service =
        new JourneyChatService(properties, contentClient, emotionalClient, quotaClient, new ObjectMapper());

    var result =
        service.reply(
            "Bearer token",
            USER,
            "Quero conversar sobre meu dia.",
            List.of(),
            null);

    assertTrue(result.fallbackUsed());
    assertEquals(Boolean.TRUE, result.quotaLimited());
    assertEquals(Boolean.TRUE, result.rewardedAdAvailable());
    assertTrue(result.limitMessage().contains("limite gratuito"));
  }
}
