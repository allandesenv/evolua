package com.evolua.auth.application;

import com.evolua.auth.domain.OAuthLoginState;
import com.evolua.auth.domain.OAuthLoginStateRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleOAuthService {
  private static final String GOOGLE_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
  private static final String GOOGLE_TOKEN_URI = "https://oauth2.googleapis.com/token";
  private static final String GOOGLE_USERINFO_URI = "https://openidconnect.googleapis.com/v1/userinfo";

  private final AuthService authService;
  private final OAuthLoginStateRepository oAuthLoginStateRepository;
  private final RestClient restClient;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String allowedFrontendOriginPatternsProperty;

  public GoogleOAuthService(
      AuthService authService,
      OAuthLoginStateRepository oAuthLoginStateRepository,
      @Value("${app.oauth.google.client-id:}") String clientId,
      @Value("${app.oauth.google.client-secret:}") String clientSecret,
      @Value("${app.oauth.google.redirect-uri:}") String redirectUri,
      @Value("${app.oauth.google.allowed-frontend-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
          String allowedFrontendOriginPatternsProperty) {
    this.authService = authService;
    this.oAuthLoginStateRepository = oAuthLoginStateRepository;
    this.restClient = RestClient.builder().build();
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
    this.allowedFrontendOriginPatternsProperty = allowedFrontendOriginPatternsProperty;
  }

  @Transactional
  public URI buildAuthorizationRedirect(String frontendRedirectUri) {
    ensureConfigured();
    validateFrontendRedirectUri(frontendRedirectUri);
    oAuthLoginStateRepository.deleteAllExpiredBefore(Instant.now());

    var state = UUID.randomUUID().toString().replace("-", "");
    oAuthLoginStateRepository.save(
        new OAuthLoginState(
            null,
            state,
            frontendRedirectUri,
            Instant.now(),
            Instant.now().plusSeconds(300)));

    return UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URI)
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", redirectUri)
        .queryParam("response_type", "code")
        .queryParam("scope", "openid email profile")
        .queryParam("access_type", "offline")
        .queryParam("prompt", "consent")
        .queryParam("state", state)
        .encode()
        .build()
        .toUri();
  }

  @Transactional
  public URI handleCallback(String code, String state, String error) {
    ensureConfigured();
    if (state == null || state.isBlank()) {
      throw new IllegalArgumentException("Missing OAuth state");
    }

    var loginState =
        oAuthLoginStateRepository
            .findByState(state)
            .orElseThrow(() -> new IllegalArgumentException("Invalid OAuth state"));

    oAuthLoginStateRepository.deleteByState(state);

    if (loginState.expiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("OAuth state expired");
    }

    if (error != null && !error.isBlank()) {
      return buildFrontendRedirect(loginState.frontendRedirectUri(), Map.of("error", error));
    }

    if (code == null || code.isBlank()) {
      return buildFrontendRedirect(
          loginState.frontendRedirectUri(), Map.of("error", "google_code_missing"));
    }

    var tokenResponse = exchangeGoogleAuthorizationCode(code);
    var userInfo = fetchGoogleUser(tokenResponse.accessToken());
    var user =
        authService.findOrCreateGoogleUser(
            userInfo.sub(), userInfo.email(), userInfo.name(), userInfo.picture());
    var authorizationCode = authService.createAuthorizationCodeForUser(user.userId());

    return buildFrontendRedirect(
        loginState.frontendRedirectUri(), Map.of("code", authorizationCode));
  }

  private GoogleTokenResponse exchangeGoogleAuthorizationCode(String code) {
    var body = new LinkedMultiValueMap<String, String>();
    body.add("code", code);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("redirect_uri", redirectUri);
    body.add("grant_type", "authorization_code");

    var response =
        restClient
            .post()
            .uri(GOOGLE_TOKEN_URI)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .retrieve()
            .body(GoogleTokenResponse.class);

    if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
      throw new IllegalArgumentException("Google token exchange failed");
    }

    return response;
  }

  private GoogleUserInfo fetchGoogleUser(String accessToken) {
    var userInfo =
        restClient
            .get()
            .uri(GOOGLE_USERINFO_URI)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .body(GoogleUserInfo.class);

    if (userInfo == null || userInfo.email() == null || userInfo.email().isBlank()) {
      throw new IllegalArgumentException("Google user info unavailable");
    }

    return userInfo;
  }

  private URI buildFrontendRedirect(String frontendRedirectUri, Map<String, String> queryParams) {
    var builder = UriComponentsBuilder.fromUriString(frontendRedirectUri);
    queryParams.forEach(builder::queryParam);
    return builder.build(true).toUri();
  }

  private void ensureConfigured() {
    if (clientId.isBlank() || clientSecret.isBlank() || redirectUri.isBlank()) {
      throw new IllegalStateException("Google OAuth is not configured");
    }
  }

  private void validateFrontendRedirectUri(String frontendRedirectUri) {
    URI uri;
    try {
      uri = URI.create(frontendRedirectUri);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Invalid frontend redirect URI", exception);
    }

    if (uri.getScheme() == null || uri.getHost() == null) {
      throw new IllegalArgumentException("Invalid frontend redirect URI");
    }

    var origin = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
    var allowed =
        Arrays.stream(allowedFrontendOriginPatternsProperty.split(","))
            .map(String::trim)
            .filter(pattern -> !pattern.isBlank())
            .anyMatch(pattern -> PatternMatchUtils.simpleMatch(pattern, origin));

    if (!allowed) {
      throw new IllegalArgumentException("Frontend redirect URI not allowed");
    }
  }

  public record GoogleTokenResponse(
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("token_type") String tokenType,
      @JsonProperty("scope") String scope) {}

  public record GoogleUserInfo(String sub, String email, String name, String picture) {}
}
