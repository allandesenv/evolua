package com.evolua.content.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailMediaLink;
import com.evolua.content.domain.TrailRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

class TrailServiceTest {
  @Test
  void shouldUpdateCatalogTrailAndPreserveMetadata() {
    var repository = new InMemoryTrailRepository();
    var service = new TrailService(repository);

    var updated =
        service.update(
            1L,
            "Titulo editado",
            "Resumo editado",
            "Conteudo editado",
            "foco",
            true,
            List.of(new TrailMediaLink("Video", "https://example.com/video", "external")));

    assertThat(updated.title()).isEqualTo("Titulo editado");
    assertThat(updated.summary()).isEqualTo("Resumo editado");
    assertThat(updated.premium()).isTrue();
    assertThat(updated.userId()).isEqualTo("admin");
    assertThat(updated.privateTrail()).isFalse();
    assertThat(updated.createdAt()).isEqualTo(repository.createdAt);
    assertThat(updated.mediaLinks()).hasSize(1);
  }

  @Test
  void shouldDeleteCatalogTrail() {
    var repository = new InMemoryTrailRepository();
    var service = new TrailService(repository);

    service.delete(1L);

    assertThat(repository.deletedId).isEqualTo(1L);
  }

  @Test
  void shouldRejectPrivateTrailManagement() {
    var repository = new InMemoryTrailRepository();
    var service = new TrailService(repository);

    assertThatThrownBy(
            () -> service.delete(2L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("403 FORBIDDEN");
  }

  private static class InMemoryTrailRepository implements TrailRepository {
    private final Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
    private final Map<Long, Trail> trails = new HashMap<>();
    private Long deletedId;

    private InMemoryTrailRepository() {
      trails.put(
          1L,
          new Trail(
              1L,
              "admin",
              "Titulo",
              "Resumo antigo",
              "Conteudo antigo",
              "bem-estar",
              false,
              false,
              false,
              false,
              null,
              null,
              List.of(),
              createdAt));
      trails.put(
          2L,
          new Trail(
              2L,
              "user-1",
              "Jornada privada",
              "Resumo privado",
              "Conteudo privado",
              "ansiedade",
              false,
              true,
              true,
              true,
              "journey-key",
              "neuro",
              List.of(),
              createdAt));
    }

    @Override
    public Trail save(Trail item) {
      trails.put(item.id(), item);
      return item;
    }

    @Override
    public Trail findById(Long id) {
      return trails.get(id);
    }

    @Override
    public void deleteById(Long id) {
      deletedId = id;
      trails.remove(id);
    }

    @Override
    public Page<Trail> findAll(
        String userId, Pageable pageable, String search, String category, Boolean premium) {
      return Page.empty();
    }

    @Override
    public Trail findActiveJourneyByUserId(String userId) {
      return null;
    }

    @Override
    public Trail findActiveJourneyByUserIdAndJourneyKey(String userId, String journeyKey) {
      return null;
    }

    @Override
    public void deactivateActiveJourneys(String userId) {}
  }
}
