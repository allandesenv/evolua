package com.evolua.content.infrastructure.persistence;

import com.evolua.content.domain.TrailProgress;
import com.evolua.content.domain.TrailProgressRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TrailProgressPersistenceAdapter implements TrailProgressRepository {
  private static final TypeReference<List<Integer>> INDEXES_TYPE = new TypeReference<>() {};

  private final TrailProgressJpaRepository repository;
  private final ObjectMapper objectMapper;

  public TrailProgressPersistenceAdapter(
      TrailProgressJpaRepository repository, ObjectMapper objectMapper) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  @Override
  public TrailProgress save(TrailProgress progress) {
    var entity = new TrailProgressEntity();
    entity.setId(progress.id());
    entity.setUserId(progress.userId());
    entity.setTrailId(progress.trailId());
    entity.setCurrentStepIndex(progress.currentStepIndex());
    entity.setCompletedStepIndexes(serialize(progress.completedStepIndexes()));
    entity.setStartedAt(progress.startedAt());
    entity.setUpdatedAt(progress.updatedAt());
    entity.setCompletedAt(progress.completedAt());
    return toDomain(repository.save(entity));
  }

  @Override
  public TrailProgress findByUserIdAndTrailId(String userId, Long trailId) {
    return repository.findByUserIdAndTrailId(userId, trailId).map(this::toDomain).orElse(null);
  }

  private TrailProgress toDomain(TrailProgressEntity entity) {
    return new TrailProgress(
        entity.getId(),
        entity.getUserId(),
        entity.getTrailId(),
        entity.getCurrentStepIndex(),
        deserialize(entity.getCompletedStepIndexes()),
        entity.getStartedAt(),
        entity.getUpdatedAt(),
        entity.getCompletedAt());
  }

  private String serialize(List<Integer> indexes) {
    try {
      return objectMapper.writeValueAsString(indexes == null ? List.of() : indexes);
    } catch (Exception exception) {
      throw new IllegalArgumentException("Could not serialize trail progress");
    }
  }

  private List<Integer> deserialize(String indexes) {
    try {
      if (indexes == null || indexes.isBlank()) {
        return List.of();
      }
      return objectMapper.readValue(indexes, INDEXES_TYPE);
    } catch (Exception exception) {
      return List.of();
    }
  }
}
