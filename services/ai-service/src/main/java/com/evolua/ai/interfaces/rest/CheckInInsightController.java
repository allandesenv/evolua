package com.evolua.ai.interfaces.rest;

import com.evolua.ai.application.CheckInInsight;
import com.evolua.ai.application.CheckInInsightService;
import com.evolua.ai.application.CurrentCheckInInput;
import com.evolua.ai.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class CheckInInsightController {
  private final CheckInInsightService checkInInsightService;
  private final CurrentUserProvider currentUserProvider;

  public CheckInInsightController(
      CheckInInsightService checkInInsightService, CurrentUserProvider currentUserProvider) {
    this.checkInInsightService = checkInInsightService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping("/check-in-insights")
  @Operation(summary = "Generate well-being insight for the current check-in")
  public ResponseEntity<CheckInInsightResponse> generateInsight(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
      @Valid @RequestBody CheckInInsightRequest request) {
    CheckInInsight insight =
        checkInInsightService.generateInsight(
            authorizationHeader,
            currentUserProvider.getCurrentUser(),
            new CurrentCheckInInput(
                request.mood(),
                request.reflection(),
                request.energyLevel(),
                null));
    return ResponseEntity.ok(CheckInInsightResponse.from(insight));
  }

  public record CheckInInsightRequest(
      @NotBlank String mood,
      String reflection,
      @Min(1) @Max(10) Integer energyLevel) {}

  public record CheckInInsightResponse(
      String insight,
      String suggestedAction,
      String riskLevel,
      Long suggestedTrailId,
      String suggestedTrailTitle,
      String suggestedTrailReason,
      Boolean fallbackUsed) {
    static CheckInInsightResponse from(CheckInInsight insight) {
      return new CheckInInsightResponse(
          insight.insight(),
          insight.suggestedAction(),
          insight.riskLevel(),
          insight.suggestedTrailId(),
          insight.suggestedTrailTitle(),
          insight.suggestedTrailReason(),
          insight.fallbackUsed());
    }
  }
}
