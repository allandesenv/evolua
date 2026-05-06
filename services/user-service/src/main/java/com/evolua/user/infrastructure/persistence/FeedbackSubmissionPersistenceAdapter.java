package com.evolua.user.infrastructure.persistence;

import com.evolua.user.domain.FeedbackSubmission;
import com.evolua.user.domain.FeedbackSubmissionRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class FeedbackSubmissionPersistenceAdapter implements FeedbackSubmissionRepository {
  private final FeedbackSubmissionJpaRepository repository;

  public FeedbackSubmissionPersistenceAdapter(FeedbackSubmissionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public FeedbackSubmission save(FeedbackSubmission submission) {
    var entity = new FeedbackSubmissionEntity();
    entity.setId(submission.id());
    entity.setUserId(submission.userId());
    entity.setEmail(submission.email());
    entity.setWorkingWell(submission.workingWell());
    entity.setCouldImprove(submission.couldImprove());
    entity.setConfusingOrHard(submission.confusingOrHard());
    entity.setHelpedHow(submission.helpedHow());
    entity.setFeatureSuggestion(submission.featureSuggestion());
    entity.setContentSuggestion(submission.contentSuggestion());
    entity.setVisualSuggestion(submission.visualSuggestion());
    entity.setAiSuggestion(submission.aiSuggestion());
    entity.setProblemWhatHappened(submission.problemWhatHappened());
    entity.setProblemWhere(submission.problemWhere());
    entity.setProblemCanRepeat(submission.problemCanRepeat());
    entity.setRating(submission.rating());
    entity.setRatingComment(submission.ratingComment());
    entity.setScreenshotFileName(submission.screenshotFileName());
    entity.setStatus(submission.status());
    entity.setCreatedAt(submission.createdAt());
    return map(repository.save(entity));
  }

  @Override
  public List<FeedbackSubmission> findByUserId(String userId) {
    return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::map).toList();
  }

  @Override
  public void deleteByUserId(String userId) {
    repository.deleteByUserId(userId);
  }

  private FeedbackSubmission map(FeedbackSubmissionEntity entity) {
    return new FeedbackSubmission(
        entity.getId(),
        entity.getUserId(),
        entity.getEmail(),
        entity.getWorkingWell(),
        entity.getCouldImprove(),
        entity.getConfusingOrHard(),
        entity.getHelpedHow(),
        entity.getFeatureSuggestion(),
        entity.getContentSuggestion(),
        entity.getVisualSuggestion(),
        entity.getAiSuggestion(),
        entity.getProblemWhatHappened(),
        entity.getProblemWhere(),
        entity.getProblemCanRepeat(),
        entity.getRating(),
        entity.getRatingComment(),
        entity.getScreenshotFileName(),
        entity.getStatus(),
        entity.getCreatedAt());
  }
}
