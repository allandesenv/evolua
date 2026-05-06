package com.evolua.user.interfaces.rest;

import com.evolua.user.application.SupportService;
import com.evolua.user.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/support")
public class SupportController {
  private final SupportService supportService;
  private final CurrentUserProvider currentUserProvider;

  public SupportController(SupportService supportService, CurrentUserProvider currentUserProvider) {
    this.supportService = supportService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping("/tickets")
  @Operation(summary = "Create support ticket")
  public ResponseEntity<ApiResponse<SupportTicketResponse>> createTicket(
      @Valid @RequestBody SupportTicketRequest request) {
    var currentUser = currentUserProvider.getCurrentUser();
    var ticket =
        supportService.createTicket(
            currentUser.userId(),
            currentUser.email(),
            request.category(),
            request.subject(),
            request.message());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Support ticket created", SupportTicketResponse.from(ticket)));
  }

  @GetMapping("/config")
  @Operation(summary = "Support links and resources")
  public ResponseEntity<ApiResponse<SupportService.SupportConfig>> config() {
    return ResponseEntity.ok(ApiResponse.success(200, "Support config", supportService.config()));
  }

  @GetMapping("/status")
  @Operation(summary = "Platform support status")
  public ResponseEntity<ApiResponse<List<SupportService.SupportStatusItem>>> status() {
    return ResponseEntity.ok(ApiResponse.success(200, "Support status", supportService.status()));
  }
}
