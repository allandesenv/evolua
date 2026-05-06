package com.evolua.user.application;

import com.evolua.user.domain.FeedbackSubmission;
import com.evolua.user.domain.FeedbackSubmissionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FeedbackService {
  private static final Set<String> RATINGS =
      Set.of("MUITO_BOA", "BOA", "NEUTRA", "RUIM", "MUITO_RUIM");
  private static final int TEXT_LIMIT = 1200;
  private static final String RECEIVED_STATUS = "RECEIVED";

  private final FeedbackSubmissionRepository repository;
  private final FeedbackScreenshotStorageService screenshotStorageService;

  public FeedbackService(
      FeedbackSubmissionRepository repository,
      FeedbackScreenshotStorageService screenshotStorageService) {
    this.repository = repository;
    this.screenshotStorageService = screenshotStorageService;
  }

  @Transactional
  public FeedbackSubmission create(
      String userId, String email, FeedbackSubmissionInput input, MultipartFile screenshot) {
    var normalized =
        new FeedbackSubmissionInput(
            text(input == null ? null : input.workingWell()),
            text(input == null ? null : input.couldImprove()),
            text(input == null ? null : input.confusingOrHard()),
            text(input == null ? null : input.helpedHow()),
            text(input == null ? null : input.featureSuggestion()),
            text(input == null ? null : input.contentSuggestion()),
            text(input == null ? null : input.visualSuggestion()),
            text(input == null ? null : input.aiSuggestion()),
            text(input == null ? null : input.problemWhatHappened()),
            text(input == null ? null : input.problemWhere()),
            text(input == null ? null : input.problemCanRepeat()),
            rating(input == null ? null : input.rating()),
            text(input == null ? null : input.ratingComment()));

    if (!hasMeaningfulFeedback(normalized)) {
      throw new IllegalArgumentException("Conte pelo menos uma percepcao ou escolha uma avaliacao.");
    }

    var screenshotFileName =
        screenshot == null || screenshot.isEmpty() ? null : screenshotStorageService.store(userId, screenshot);
    var createdAt = Instant.now();
    return repository.save(
        new FeedbackSubmission(
            null,
            userId,
            email,
            normalized.workingWell(),
            normalized.couldImprove(),
            normalized.confusingOrHard(),
            normalized.helpedHow(),
            normalized.featureSuggestion(),
            normalized.contentSuggestion(),
            normalized.visualSuggestion(),
            normalized.aiSuggestion(),
            normalized.problemWhatHappened(),
            normalized.problemWhere(),
            normalized.problemCanRepeat(),
            normalized.rating(),
            normalized.ratingComment(),
            screenshotFileName,
            RECEIVED_STATUS,
            createdAt));
  }

  public List<FeedbackSubmission> findByUserId(String userId) {
    return repository.findByUserId(userId);
  }

  @Transactional
  public void deleteByUserId(String userId) {
    repository.deleteByUserId(userId);
    screenshotStorageService.deleteForUser(userId);
  }

  private boolean hasMeaningfulFeedback(FeedbackSubmissionInput input) {
    return input.rating() != null
        || input.workingWell() != null
        || input.couldImprove() != null
        || input.confusingOrHard() != null
        || input.helpedHow() != null
        || input.featureSuggestion() != null
        || input.contentSuggestion() != null
        || input.visualSuggestion() != null
        || input.aiSuggestion() != null
        || input.problemWhatHappened() != null
        || input.problemWhere() != null
        || input.problemCanRepeat() != null
        || input.ratingComment() != null;
  }

  private String text(String value) {
    if (value == null || value.trim().isBlank()) {
      return null;
    }
    var normalized = value.trim();
    if (normalized.length() > TEXT_LIMIT) {
      throw new IllegalArgumentException("Use textos com ate 1200 caracteres por campo.");
    }
    return normalized;
  }

  private String rating(String value) {
    if (value == null || value.trim().isBlank()) {
      return null;
    }
    var normalized = value.trim().toUpperCase();
    if (!RATINGS.contains(normalized)) {
      throw new IllegalArgumentException("Avaliacao invalida.");
    }
    return normalized;
  }
}
