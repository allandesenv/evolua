package com.evolua.emotional.infrastructure.persistence;

import com.evolua.emotional.domain.CheckIn;
import com.evolua.emotional.domain.CheckInRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CheckInPersistenceAdapter implements CheckInRepository {
  private final CheckInJpaRepository repository;

  public CheckInPersistenceAdapter(CheckInJpaRepository repository) {
    this.repository = repository;
  }

  public CheckIn save(CheckIn item) {
    CheckInEntity entity = new CheckInEntity();
    entity.setId(item.id());
    entity.setUserId(item.userId());
    entity.setMood(item.mood());
    entity.setReflection(item.reflection());
    entity.setEnergyLevel(item.energyLevel());
    entity.setRecommendedPractice(item.recommendedPractice());
    entity.setCreatedAt(item.createdAt());
    CheckInEntity saved = repository.save(entity);
    return new CheckIn(
        saved.getId(),
        saved.getUserId(),
        saved.getMood(),
        saved.getReflection(),
        saved.getEnergyLevel(),
        saved.getRecommendedPractice(),
        saved.getCreatedAt());
  }

  public Page<CheckIn> findAllByUserId(
      String userId,
      Pageable pageable,
      String search,
      String mood,
      Integer energyMin,
      Integer energyMax,
      Instant from,
      Instant to) {
    Specification<CheckInEntity> specification =
        Specification.where((root, query, cb) -> cb.equal(root.get("userId"), userId));

    if (search != null && !search.isBlank()) {
      var normalized = "%" + search.toLowerCase() + "%";
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("reflection")), normalized),
                      cb.like(cb.lower(root.get("recommendedPractice")), normalized),
                      cb.like(cb.lower(root.get("mood")), normalized)));
    }

    if (mood != null && !mood.isBlank()) {
      var normalizedMood = mood.toLowerCase();
      specification =
          specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("mood")), normalizedMood));
    }

    if (energyMin != null) {
      specification =
          specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("energyLevel"), energyMin));
    }

    if (energyMax != null) {
      specification =
          specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("energyLevel"), energyMax));
    }

    if (from != null) {
      specification =
          specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
    }

    if (to != null) {
      specification =
          specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
    }

    return repository.findAll(specification, pageable)
        .map(
            saved ->
                new CheckIn(
                    saved.getId(),
                    saved.getUserId(),
                    saved.getMood(),
                    saved.getReflection(),
                    saved.getEnergyLevel(),
                    saved.getRecommendedPractice(),
                    saved.getCreatedAt()));
  }
}
