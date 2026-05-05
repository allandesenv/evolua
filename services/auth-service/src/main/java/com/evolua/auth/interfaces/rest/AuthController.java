package com.evolua.auth.interfaces.rest;

import com.evolua.auth.application.AuthService;
import com.evolua.auth.application.GoogleOAuthService;
import com.evolua.auth.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthController {
  private final AuthService authService;
  private final GoogleOAuthService googleOAuthService;
  private final CurrentUserProvider currentUserProvider;

  public AuthController(
      AuthService authService,
      GoogleOAuthService googleOAuthService,
      CurrentUserProvider currentUserProvider) {
    this.authService = authService;
    this.googleOAuthService = googleOAuthService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping("/v1/public/auth/register")
  @Operation(summary = "Register user")
  public ResponseEntity<ApiResponse<AuthUserResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    var user = authService.register(request.email(), request.password(), request.displayName());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Created", toAuthUserResponse(user)));
  }

  @PostMapping("/v1/public/auth/login")
  @Operation(summary = "Login user")
  public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
    var tokens = authService.login(request.email(), request.password());
    return ResponseEntity.ok(ApiResponse.success(200, "Logged in", toTokenResponse(tokens)));
  }

  @PostMapping("/v1/public/auth/refresh")
  @Operation(summary = "Refresh tokens")
  public ResponseEntity<ApiResponse<TokenResponse>> refresh(
      @Valid @RequestBody RefreshRequest request) {
    var tokens = authService.refresh(request.refreshToken());
    return ResponseEntity.ok(ApiResponse.success(200, "Refreshed", toTokenResponse(tokens)));
  }

  @GetMapping("/v1/public/auth/google/start")
  @Operation(summary = "Start Google OAuth login")
  public ResponseEntity<Void> startGoogleLogin(
      @RequestParam("frontendRedirectUri") String frontendRedirectUri) {
    return redirect(googleOAuthService.buildAuthorizationRedirect(frontendRedirectUri));
  }

  @GetMapping("/auth/google/callback")
  @Operation(summary = "Google OAuth callback")
  public ResponseEntity<Void> googleCallback(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "state", required = false) String state,
      @RequestParam(value = "error", required = false) String error) {
    return redirect(googleOAuthService.handleCallback(code, state, error));
  }

  @PostMapping("/v1/public/auth/google/exchange")
  @Operation(summary = "Exchange Google authorization code")
  public ResponseEntity<ApiResponse<TokenResponse>> exchangeGoogleAuthorizationCode(
      @Valid @RequestBody GoogleExchangeRequest request) {
    var tokens = authService.exchangeAuthorizationCode(request.code());
    return ResponseEntity.ok(
        ApiResponse.success(200, "Logged in with Google", toTokenResponse(tokens)));
  }

  @GetMapping("/v1/auth/me")
  @Operation(summary = "Current account")
  public ResponseEntity<ApiResponse<AuthUserResponse>> me() {
    var user = authService.me(currentUserProvider.getCurrentUser().userId());
    return ResponseEntity.ok(ApiResponse.success(200, "Current user", toAuthUserResponse(user)));
  }

  @PatchMapping("/v1/auth/me/password")
  @Operation(summary = "Change current account password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request) {
    authService.changePassword(
        currentUserProvider.getCurrentUser().userId(),
        request.currentPassword(),
        request.newPassword());
    return ResponseEntity.ok(ApiResponse.success(200, "Password changed", null));
  }

  @PostMapping("/v1/auth/me/sessions/revoke")
  @Operation(summary = "Revoke active sessions")
  public ResponseEntity<ApiResponse<Void>> revokeSessions() {
    authService.revokeSessions(currentUserProvider.getCurrentUser().userId());
    return ResponseEntity.ok(ApiResponse.success(200, "Sessions revoked", null));
  }

  @PostMapping("/v1/auth/me/deactivate")
  @Operation(summary = "Deactivate current account")
  public ResponseEntity<ApiResponse<Void>> deactivate(
      @Valid @RequestBody AccountConfirmationRequest request) {
    authService.deactivate(currentUserProvider.getCurrentUser().userId(), request.confirmation());
    return ResponseEntity.ok(ApiResponse.success(200, "Account deactivated", null));
  }

  @DeleteMapping("/v1/auth/me")
  @Operation(summary = "Delete current account")
  public ResponseEntity<ApiResponse<Void>> deleteAccount(
      @Valid @RequestBody DeleteAccountRequest request) {
    authService.deleteAccount(
        currentUserProvider.getCurrentUser().userId(),
        request.confirmation(),
        request.currentPassword());
    return ResponseEntity.ok(ApiResponse.success(200, "Account deleted", null));
  }

  private ResponseEntity<Void> redirect(URI location) {
    return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, location.toString()).build();
  }

  private AuthUserResponse toAuthUserResponse(com.evolua.auth.domain.AuthUser user) {
    return new AuthUserResponse(
        user.userId(),
        user.email(),
        user.displayName(),
        user.avatarUrl(),
        user.roles());
  }

  private TokenResponse toTokenResponse(com.evolua.auth.application.AuthTokens tokens) {
    var user = tokens.user();
    return new TokenResponse(
        tokens.accessToken(),
        tokens.refreshToken(),
        user.userId(),
        user.email(),
        user.displayName(),
        user.avatarUrl(),
        user.roles());
  }

  public record RegisterRequest(@Email String email, @NotBlank String password, @NotBlank String displayName) {}

  public record LoginRequest(@Email String email, @NotBlank String password) {}

  public record RefreshRequest(@NotBlank String refreshToken) {}

  public record GoogleExchangeRequest(@NotBlank String code) {}

  public record ChangePasswordRequest(
      @NotBlank String currentPassword, @NotBlank String newPassword) {}

  public record AccountConfirmationRequest(@NotBlank String confirmation) {}

  public record DeleteAccountRequest(@NotBlank String confirmation, String currentPassword) {}

  public record TokenResponse(
      String accessToken,
      String refreshToken,
      String userId,
      String email,
      String displayName,
      String avatarUrl,
      List<String> roles) {}

  public record AuthUserResponse(
      String userId, String email, String displayName, String avatarUrl, List<String> roles) {}
}
