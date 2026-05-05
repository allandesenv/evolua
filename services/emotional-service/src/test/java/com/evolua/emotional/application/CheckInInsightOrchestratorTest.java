package com.evolua.emotional.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evolua.emotional.domain.CheckIn;
import com.evolua.emotional.domain.CheckInRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckInInsightOrchestratorTest {
  @Test
  void defaultFlowDoesNotCallAiService() {
    var repository = mock(CheckInRepository.class);
    when(repository.findRecentByUserId(any(), any())).thenReturn(List.of());
    when(repository.save(any(CheckIn.class)))
        .thenAnswer(
            invocation -> {
              CheckIn item = invocation.getArgument(0);
              return new CheckIn(
                  10L,
                  item.userId(),
                  item.mood(),
                  item.reflection(),
                  item.energyLevel(),
                  item.recommendedPractice(),
                  item.createdAt() == null ? Instant.now() : item.createdAt(),
                  item.emotion(),
                  item.intensity(),
                  item.energy(),
                  item.context(),
                  item.decisionTags(),
                  item.severityLevel());
            });
    var subscriptionAccessClient = mock(SubscriptionAccessClient.class);
    var checkInService = new CheckInService(repository, subscriptionAccessClient);
    var aiClient = mock(CheckInInsightClient.class);
    var journeyClient = mock(JourneyTrailClient.class);
    var engine =
        new EmotionalCheckinEngine(
            new CheckinRuleEvaluator(new EmotionalStateCatalog(), true, true));
    var orchestrator =
        new CheckInInsightOrchestrator(
            checkInService, aiClient, journeyClient, engine, false);

    var result =
        orchestrator.createWithInsight(
            "Bearer token",
            "user-1",
            "ansioso",
            "muita coisa",
            9,
            "ansioso",
            8,
            "alta",
            "trabalho",
            "muita coisa");

    assertNotNull(result.aiInsight());
    assertEquals("mente acelerada", result.aiInsight().emotionalStateLabel());
    assertEquals("ansioso", result.checkIn().emotion());
    assertEquals("alta", result.checkIn().energy());
    verify(aiClient, never()).generateInsight(any(), any());
    verify(journeyClient, never()).upsertCurrentJourney(any(), any(), any());
  }
}
