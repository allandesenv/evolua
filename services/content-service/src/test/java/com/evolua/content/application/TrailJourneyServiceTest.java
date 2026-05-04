package com.evolua.content.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailProgress;
import com.evolua.content.domain.TrailProgressRepository;
import com.evolua.content.domain.TrailRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class TrailJourneyServiceTest {
  @Test
  void shouldStartJourneyOnlyOnceAndCompleteSteps() {
    var trailRepository = new InMemoryTrailRepository();
    var progressRepository = new InMemoryProgressRepository();
    var service =
        new TrailJourneyService(trailRepository, progressRepository, new TrailStepDeriver());

    var started = service.startJourney("user-1", 1L);
    var startedAgain = service.startJourney("user-1", 1L);
    var afterFirstStep = service.completeStep("user-1", 1L, 0);

    assertThat(started.progress().currentStepIndex()).isZero();
    assertThat(startedAgain.progress().id()).isEqualTo(started.progress().id());
    assertThat(afterFirstStep.progress().completedStepIndexes()).containsExactly(0);
    assertThat(afterFirstStep.progress().currentStepIndex()).isEqualTo(1);
    assertThat(afterFirstStep.progressPercent()).isEqualTo(33);
  }

  @Test
  void shouldMarkJourneyCompletedWhenLastStepIsDone() {
    var trailRepository = new InMemoryTrailRepository();
    var progressRepository = new InMemoryProgressRepository();
    var service =
        new TrailJourneyService(trailRepository, progressRepository, new TrailStepDeriver());

    service.startJourney("user-1", 1L);
    service.completeStep("user-1", 1L, 0);
    service.completeStep("user-1", 1L, 1);
    var completed = service.completeStep("user-1", 1L, 2);

    assertThat(completed.progressPercent()).isEqualTo(100);
    assertThat(completed.progress().completedAt()).isNotNull();
    assertThat(completed.steps()).allMatch(step -> "completed".equals(step.status()));
  }

  @Test
  void shouldKeepPrivateAndCatalogTrailProgressSeparated() {
    var trailRepository = new InMemoryTrailRepository();
    var progressRepository = new InMemoryProgressRepository();
    var service =
        new TrailJourneyService(trailRepository, progressRepository, new TrailStepDeriver());

    service.startJourney("user-1", 1L);
    service.startJourney("user-1", 2L);
    service.completeStep("user-1", 2L, 0);

    var privateJourney = service.getJourney("user-1", 1L);
    var catalogJourney = service.getJourney("user-1", 2L);

    assertThat(privateJourney.progressPercent()).isZero();
    assertThat(privateJourney.progress().completedStepIndexes()).isEmpty();
    assertThat(catalogJourney.progressPercent()).isEqualTo(33);
    assertThat(catalogJourney.progress().completedStepIndexes()).containsExactly(0);
  }

  private static class InMemoryTrailRepository implements TrailRepository {
    private final Map<Long, Trail> trails =
        Map.of(
            1L,
            new Trail(
                1L,
                "user-1",
                "Jornada de foco",
                "Resumo",
                """
                ## Respirar
                Volte para o corpo.
                ## Escolher
                Escolha uma acao.
                ## Integrar
                Registre o aprendizado.
                """,
                "foco",
                false,
                true,
                true,
                true,
                "focus",
                "neuro",
                List.of(),
                Instant.now()),
            2L,
            new Trail(
                2L,
                "admin",
                "Presenca em 10 minutos",
                "Resumo catalogo",
                """
                ## Observar
                Volte para o agora.
                ## Praticar
                Escolha uma microacao.
                ## Revisar
                Registre o aprendizado.
                """,
                "presenca",
                false,
                false,
                false,
                false,
                null,
                null,
                List.of(),
                Instant.now()));

    @Override
    public Trail save(Trail item) {
      return item;
    }

    @Override
    public Trail findById(Long id) {
      return trails.get(id);
    }

    @Override
    public void deleteById(Long id) {}

    @Override
    public Page<Trail> findAll(
        String userId, Pageable pageable, String search, String category, Boolean premium) {
      return Page.empty();
    }

    @Override
    public Trail findActiveJourneyByUserId(String userId) {
      return trails.get(1L);
    }

    @Override
    public Trail findActiveJourneyByUserIdAndJourneyKey(String userId, String journeyKey) {
      return trails.get(1L);
    }

    @Override
    public void deactivateActiveJourneys(String userId) {}
  }

  private static class InMemoryProgressRepository implements TrailProgressRepository {
    private final Map<String, TrailProgress> store = new HashMap<>();
    private long sequence = 1L;

    @Override
    public TrailProgress save(TrailProgress progress) {
      var saved =
          new TrailProgress(
              progress.id() == null ? sequence++ : progress.id(),
              progress.userId(),
              progress.trailId(),
              progress.currentStepIndex(),
              progress.completedStepIndexes(),
              progress.startedAt(),
              progress.updatedAt(),
              progress.completedAt());
      store.put(key(saved.userId(), saved.trailId()), saved);
      return saved;
    }

    @Override
    public TrailProgress findByUserIdAndTrailId(String userId, Long trailId) {
      return store.get(key(userId, trailId));
    }

    private String key(String userId, Long trailId) {
      return userId + ":" + trailId;
    }
  }
}
