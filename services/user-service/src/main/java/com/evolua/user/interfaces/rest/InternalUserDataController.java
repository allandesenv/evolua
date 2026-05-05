package com.evolua.user.interfaces.rest;

import com.evolua.user.application.PrivacySettingsService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/internal/users")
public class InternalUserDataController {
  private final PrivacySettingsService privacySettingsService;
  private final String internalToken;

  public InternalUserDataController(
      PrivacySettingsService privacySettingsService,
      @Value("${app.account.internal-token:change-me-internal-token}") String internalToken) {
    this.privacySettingsService = privacySettingsService;
    this.internalToken = internalToken;
  }

  @PostMapping("/delete-data")
  @Operation(summary = "Delete user data for an internal caller")
  public ResponseEntity<ApiResponse<Void>> deleteUserData(
      @RequestHeader(name = "X-Internal-Token", required = false) String token,
      @Valid @RequestBody DeleteUserDataRequest request) {
    validateInternalToken(token);
    privacySettingsService.deleteUserData(request.userId());
    return ResponseEntity.ok(ApiResponse.success(200, "User data deleted", null));
  }

  private void validateInternalToken(String token) {
    if (internalToken == null || internalToken.isBlank() || !internalToken.equals(token)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal token");
    }
  }
}
