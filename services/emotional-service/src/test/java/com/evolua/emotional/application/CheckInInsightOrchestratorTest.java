package com.evolua.emotional.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evolua.emotional.application.JourneyTrailClient.JourneyTrailSummary;
import com.evolua.emotional.domain.CheckIn;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckInInsightOrchestratorTest {
  @Test
  void enrichesCheckInResponseWithPrivateJourneyTrail() {
    var checkInService = mock(CheckInService.class);
    var checkInInsightClient = mock(CheckInInsightClient.class);
    var journeyTrailClient = mock(JourneyTrailClient.class);
    var orchestrator =
        new CheckInInsightOrchestrator(checkInService, checkInInsightClient, journeyTrailClient);
    var created =
        new CheckIn(
            10L,
            "user-1",
            "ansioso",
            "Mente acelerada pelo trabalho.",
            4,
            "Escolha uma pausa curta de regulacao por 5 minutos.",
            Instant.parse("2026-05-05T12:00:00Z"));
    var updated =
        new CheckIn(
            created.id(),
            created.userId(),
            created.mood(),
            created.reflection(),
            created.energyLevel(),
            "Respire por 3 minutos e escolha uma prioridade simples.",
            created.createdAt());
    var journeyPlan =
        new CheckInAiJourneyPlan(
            "voltar-ao-eixo",
            "Voltar ao eixo",
            "Primeiro passo",
            "private",
            "Uma trilha curta para hoje.",
            "Como foi seguir o primeiro passo?");
    var draft =
        new CheckInAiGeneratedTrailDraft(
            "Voltar ao eixo",
            "Uma trilha privada para reduzir ativacao.",
            "# Voltar ao eixo\n\n## Leitura do momento\nRespire.",
            "ansiedade",
            "generated",
            List.of());
    var aiInsight =
        new CheckInAiInsight(
            "Seu corpo esta pedindo uma pausa antes de decidir.",
            updated.recommendedPractice(),
            "low",
            1L,
            "Catalogo antigo",
            "Combina com seu check-in.",
            null,
            journeyPlan,
            draft,
            false);
    var journeyTrail =
        new JourneyTrailSummary(
            42L, "Voltar ao eixo personalizado", "Trilha privada criada.", "ansiedade", "voltar-ao-eixo", "generated");
    when(checkInService.create(
            "user-1",
            "ansioso",
            "Mente acelerada pelo trabalho.",
            4,
            "Escolha uma pausa curta de regulacao por 5 minutos."))
        .thenReturn(created);
    when(checkInInsightClient.generateInsight("Bearer token", created)).thenReturn(aiInsight);
    when(journeyTrailClient.upsertCurrentJourney("Bearer token", draft, journeyPlan)).thenReturn(journeyTrail);
    when(checkInService.updateRecommendedPractice(created, updated.recommendedPractice())).thenReturn(updated);

    var result =
        orchestrator.createWithInsight(
            "Bearer token", "user-1", "ansioso", "Mente acelerada pelo trabalho.", 4);

    assertSame(updated, result.checkIn());
    assertEquals(42L, result.aiInsight().suggestedTrailId());
    assertEquals("Voltar ao eixo personalizado", result.aiInsight().suggestedTrailTitle());
    assertEquals(journeyPlan, result.aiInsight().journeyPlan());
    assertEquals(draft, result.aiInsight().generatedTrailDraft());
    verify(journeyTrailClient).upsertCurrentJourney("Bearer token", draft, journeyPlan);
  }

  @Test
  void keepsOriginalInsightWhenPrivateJourneyTrailCannotBeCreated() {
    var checkInService = mock(CheckInService.class);
    var checkInInsightClient = mock(CheckInInsightClient.class);
    var journeyTrailClient = mock(JourneyTrailClient.class);
    var orchestrator =
        new CheckInInsightOrchestrator(checkInService, checkInInsightClient, journeyTrailClient);
    var created =
        new CheckIn(
            11L,
            "user-1",
            "calmo",
            "",
            7,
            "Siga com uma unica acao leve para sustentar o resto do dia.",
            Instant.parse("2026-05-05T12:30:00Z"));
    var aiInsight =
        new CheckInAiInsight(
            "Check-in salvo com uma orientacao simples.",
            created.recommendedPractice(),
            "low",
            null,
            null,
            "A trilha detalhada ficou indisponivel agora.",
            null,
            null,
            null,
            true);
    when(checkInService.create(
            "user-1",
            "calmo",
            "",
            7,
            "Siga com uma unica acao leve para sustentar o resto do dia."))
        .thenReturn(created);
    when(checkInInsightClient.generateInsight("Bearer token", created)).thenReturn(aiInsight);
    when(journeyTrailClient.upsertCurrentJourney("Bearer token", null, null)).thenReturn(null);

    var result = orchestrator.createWithInsight("Bearer token", "user-1", "calmo", "", 7);

    assertSame(created, result.checkIn());
    assertSame(aiInsight, result.aiInsight());
    verify(journeyTrailClient).upsertCurrentJourney("Bearer token", null, null);
  }
}
