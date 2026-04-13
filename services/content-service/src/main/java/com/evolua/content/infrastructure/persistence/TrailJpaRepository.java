package com.evolua.content.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TrailJpaRepository
    extends JpaRepository<TrailEntity, Long>, JpaSpecificationExecutor<TrailEntity> {
  Optional<TrailEntity> findFirstByUserIdAndActiveJourneyTrueOrderByCreatedAtDesc(String userId);

  Optional<TrailEntity> findFirstByUserIdAndJourneyKeyAndActiveJourneyTrueOrderByCreatedAtDesc(
      String userId, String journeyKey);

  @Modifying
  @Transactional
  @Query("update TrailEntity t set t.activeJourney = false where t.userId = :userId and t.activeJourney = true")
  void deactivateActiveJourneys(@Param("userId") String userId);
}
