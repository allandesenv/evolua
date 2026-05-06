package com.evolua.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.evolua.user.domain.FeedbackSubmission;
import com.evolua.user.domain.FeedbackSubmissionRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

class FeedbackServiceTest {
  private FeedbackSubmissionRepository repository;
  private FeedbackScreenshotStorageService screenshotStorageService;
  private FeedbackService service;

  @BeforeEach
  void setUp() {
    repository = mock(FeedbackSubmissionRepository.class);
    screenshotStorageService = mock(FeedbackScreenshotStorageService.class);
    service = new FeedbackService(repository, screenshotStorageService);
    when(repository.save(any(FeedbackSubmission.class)))
        .thenAnswer(
            invocation -> {
              var submission = invocation.getArgument(0, FeedbackSubmission.class);
              return new FeedbackSubmission(
                  7L,
                  submission.userId(),
                  submission.email(),
                  submission.workingWell(),
                  submission.couldImprove(),
                  submission.confusingOrHard(),
                  submission.helpedHow(),
                  submission.featureSuggestion(),
                  submission.contentSuggestion(),
                  submission.visualSuggestion(),
                  submission.aiSuggestion(),
                  submission.problemWhatHappened(),
                  submission.problemWhere(),
                  submission.problemCanRepeat(),
                  submission.rating(),
                  submission.ratingComment(),
                  submission.screenshotFileName(),
                  submission.status(),
                  submission.createdAt());
            });
  }

  @Test
  void createShouldAcceptRatingOnly() {
    var saved =
        service.create(
            "user-1",
            "leo@evolua.local",
            new FeedbackSubmissionInput(
                null, null, null, null, null, null, null, null, null, null, null, "boa", null),
            null);

    assertThat(saved.id()).isEqualTo(7L);
    assertThat(saved.rating()).isEqualTo("BOA");
    assertThat(saved.status()).isEqualTo("RECEIVED");
    assertThat(saved.screenshotFileName()).isNull();
    verifyNoInteractions(screenshotStorageService);
  }

  @Test
  void createShouldTrimTextAndStoreScreenshot() {
    var screenshot = new MockMultipartFile("screenshot", "bug.png", "image/png", new byte[] {1, 2, 3});
    when(screenshotStorageService.store("user-1", screenshot)).thenReturn("user-1-file.png");

    service.create(
        "user-1",
        "leo@evolua.local",
        new FeedbackSubmissionInput(
            "  Funcionou bem  ",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "A tela travou",
            null,
            null,
            null,
            null),
        screenshot);

    var captor = ArgumentCaptor.forClass(FeedbackSubmission.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().workingWell()).isEqualTo("Funcionou bem");
    assertThat(captor.getValue().problemWhatHappened()).isEqualTo("A tela travou");
    assertThat(captor.getValue().screenshotFileName()).isEqualTo("user-1-file.png");
    assertThat(captor.getValue().createdAt()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  void createShouldRejectEmptyFeedback() {
    assertThatThrownBy(
            () ->
                service.create(
                    "user-1",
                    "leo@evolua.local",
                    new FeedbackSubmissionInput(
                        null, null, null, null, null, null, null, null, null, null, null, null, null),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Conte pelo menos uma percepcao ou escolha uma avaliacao.");
  }

  @Test
  void createShouldRejectInvalidRating() {
    assertThatThrownBy(
            () ->
                service.create(
                    "user-1",
                    "leo@evolua.local",
                    new FeedbackSubmissionInput(
                        null, null, null, null, null, null, null, null, null, null, null, "excelente", null),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Avaliacao invalida.");
  }

  @Test
  void createShouldRejectLongText() {
    assertThatThrownBy(
            () ->
                service.create(
                    "user-1",
                    "leo@evolua.local",
                    new FeedbackSubmissionInput(
                        "a".repeat(1201),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Use textos com ate 1200 caracteres por campo.");
  }

  @Test
  void deleteByUserIdShouldRemoveRecordsAndScreenshots() {
    service.deleteByUserId("user-1");

    verify(repository).deleteByUserId("user-1");
    verify(screenshotStorageService).deleteForUser("user-1");
  }
}
