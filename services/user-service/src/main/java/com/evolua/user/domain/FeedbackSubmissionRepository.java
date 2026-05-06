package com.evolua.user.domain;

import java.util.List;

public interface FeedbackSubmissionRepository {
  FeedbackSubmission save(FeedbackSubmission submission);

  List<FeedbackSubmission> findByUserId(String userId);

  void deleteByUserId(String userId);
}
