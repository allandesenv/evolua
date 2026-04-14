package com.evolua.ai.interfaces.rest;

import com.evolua.ai.application.JourneyChatMessage;
import com.evolua.ai.application.JourneyChatResponse;
import com.evolua.ai.application.JourneyChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai")
public class JourneyChatController {
  private final JourneyChatService journeyChatService;

  public JourneyChatController(JourneyChatService journeyChatService) {
    this.journeyChatService = journeyChatService;
  }

  @PostMapping("/journey-chat")
  @Operation(summary = "Reply to the current private AI journey conversation")
  public ResponseEntity<JourneyChatResponse> reply(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
      @Valid @RequestBody JourneyChatRequest request) {
    return ResponseEntity.ok(
        journeyChatService.reply(
            authorizationHeader,
            request.message(),
            request.conversationHistory() == null ? List.of() : request.conversationHistory(),
            request.trailId()));
  }

  public record JourneyChatRequest(
      @NotBlank @Size(max = 1200) String message,
      Long trailId,
      @Size(max = 6) List<JourneyChatMessage> conversationHistory) {}
}
