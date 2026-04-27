package com.evolua.content.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrailProgressJpaRepository extends JpaRepository<TrailProgressEntity, Long> {
  Optional<TrailProgressEntity> findByUserIdAndTrailId(String userId, Long trailId);
}
