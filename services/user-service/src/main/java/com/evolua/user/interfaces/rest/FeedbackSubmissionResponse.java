package com.evolua.user.interfaces.rest;

import com.evolua.user.domain.FeedbackSubmission;
import java.time.Instant;

public record FeedbackSubmissionResponse(
    Long id, String status, Instant createdAt, boolean screenshotAttached) {
  public static FeedbackSubmissionResponse from(FeedbackSubmission submission) {
    return new FeedbackSubmissionResponse(
        submission.id(),
        submission.status(),
        submission.createdAt(),
        submission.screenshotFileName() != null && !submission.screenshotFileName().isBlank());
  }
}
