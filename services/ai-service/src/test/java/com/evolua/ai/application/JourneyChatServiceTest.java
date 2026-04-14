package com.evolua.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.evolua.ai.config.AiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JourneyChatServiceTest {
  @Test
  void promptContextIncludesRecentCheckInsAndJourneySections() {
    var contentClient = Mockito.mock(ContentCatalogClient.class);
    var emotionalClient = Mockito.mock(EmotionalContextClient.class);
    var service =
        new JourneyChatService(new AiProperties(), contentClient, emotionalClient, new ObjectMapper());

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
    var service =
        new JourneyChatService(properties, contentClient, emotionalClient, new ObjectMapper());

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
            "Ainda estou travado e sem clareza para comecar.",
            List.of(),
            7L);

    assertTrue(result.fallbackUsed());
    assertTrue(result.reply().contains("Dormi pouco e fiquei esgotado"));
    assertTrue(result.reply().contains("Reduzir a carga e voltar ao corpo"));
    assertTrue(result.reply().contains("Recuperacao gentil para corpo e mente"));
  }
}
