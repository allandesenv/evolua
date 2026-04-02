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
    var user = authService.register(request.email(), request.password());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", new AuthUserResponse(user.userId(), user.email(), user.roles())));
  }

  @PostMapping("/v1/public/auth/login")
  @Operation(summary = "Login user")
  public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
    var tokens = authService.login(request.email(), request.password());
    return ResponseEntity.ok(ApiResponse.success(200, "Logged in", new TokenResponse(tokens.accessToken(), tokens.refreshToken())));
  }

  @PostMapping("/v1/public/auth/refresh")
  @Operation(summary = "Refresh tokens")
  public ResponseEntity<ApiResponse<TokenResponse>> refresh(
      @Valid @RequestBody RefreshRequest request) {
    var tokens = authService.refresh(request.refreshToken());
    return ResponseEntity.ok(ApiResponse.success(200, "Refreshed", new TokenResponse(tokens.accessToken(), tokens.refreshToken())));
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
        ApiResponse.success(200, "Logged in with Google", new TokenResponse(tokens.accessToken(), tokens.refreshToken())));
  }

  @GetMapping("/v1/auth/me")
  @Operation(summary = "Current account")
  public ResponseEntity<ApiResponse<AuthUserResponse>> me() {
    var user = authService.me(currentUserProvider.getCurrentUser().userId());
    return ResponseEntity.ok(
        ApiResponse.success(200, "Current user", new AuthUserResponse(user.userId(), user.email(), user.roles())));
  }

  private ResponseEntity<Void> redirect(URI location) {
    return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, location.toString()).build();
  }

  public record RegisterRequest(@Email String email, @NotBlank String password) {}

  public record LoginRequest(@Email String email, @NotBlank String password) {}

  public record RefreshRequest(@NotBlank String refreshToken) {}

  public record GoogleExchangeRequest(@NotBlank String code) {}

  public record TokenResponse(String accessToken, String refreshToken) {}

  public record AuthUserResponse(String userId, String email, List<String> roles) {}
}
