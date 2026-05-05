package com.evolua.user.interfaces.rest;

import com.evolua.user.application.FeedbackService;
import com.evolua.user.infrastructure.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/feedback")
public class FeedbackController {
  private final FeedbackService feedbackService;
  private final CurrentUserProvider currentUserProvider;
  private final ObjectMapper objectMapper;

  public FeedbackController(
      FeedbackService feedbackService,
      CurrentUserProvider currentUserProvider,
      ObjectMapper objectMapper) {
    this.feedbackService = feedbackService;
    this.currentUserProvider = currentUserProvider;
    this.objectMapper = objectMapper;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create feedback submission")
  public ResponseEntity<ApiResponse<FeedbackSubmissionResponse>> create(
      @RequestPart("payload") String payload,
      @RequestPart(value = "screenshot", required = false) MultipartFile screenshot) {
    var currentUser = currentUserProvider.getCurrentUser();
    var request = readPayload(payload);
    var submission =
        feedbackService.create(currentUser.userId(), currentUser.email(), request.toInput(), screenshot);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Feedback received", FeedbackSubmissionResponse.from(submission)));
  }

  private FeedbackSubmissionRequest readPayload(String payload) {
    try {
      return objectMapper.readValue(payload, FeedbackSubmissionRequest.class);
    } catch (IOException exception) {
      throw new IllegalArgumentException("Payload de feedback invalido.");
    }
  }
}
