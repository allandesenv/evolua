package com.evolua.emotional.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CheckInJpaRepository
    extends JpaRepository<CheckInEntity, Long>, JpaSpecificationExecutor<CheckInEntity> {
  List<CheckInEntity> findTop30ByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
      String userId, Instant createdAt);
}
