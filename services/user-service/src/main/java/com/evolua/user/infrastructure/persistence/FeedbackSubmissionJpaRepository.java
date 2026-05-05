package com.evolua.user.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackSubmissionJpaRepository extends JpaRepository<FeedbackSubmissionEntity, Long> {
  List<FeedbackSubmissionEntity> findByUserIdOrderByCreatedAtDesc(String userId);

  void deleteByUserId(String userId);
}
