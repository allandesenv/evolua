package com.evolua.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evolua.auth.domain.OAuthLoginState;
import com.evolua.auth.domain.OAuthLoginStateRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GoogleOAuthServiceTest {
  @Test
  void callbackShouldRejectMissingState() {
    var service = buildService(mock(OAuthLoginStateRepository.class));

    assertThatThrownBy(() -> service.handleCallback("code", "", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Missing OAuth state");
  }

  @Test
  void callbackShouldRejectInvalidState() {
    var repository = mock(OAuthLoginStateRepository.class);
    when(repository.findByState("invalid")).thenReturn(Optional.empty());
    var service = buildService(repository);

    assertThatThrownBy(() -> service.handleCallback("code", "invalid", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid OAuth state");
  }

  @Test
  void callbackShouldRejectExpiredState() {
    var repository = mock(OAuthLoginStateRepository.class);
    when(repository.findByState("expired"))
        .thenReturn(
            Optional.of(
                new OAuthLoginState(
                    1L,
                    "expired",
                    "http://localhost:7359/auth/google/callback",
                    Instant.now().minusSeconds(600),
                    Instant.now().minusSeconds(1))));
    var service = buildService(repository);

    assertThatThrownBy(() -> service.handleCallback("code", "expired", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("OAuth state expired");
    verify(repository).deleteByState("expired");
  }

  @Test
  void callbackShouldReturnFrontendErrorWhenGoogleReturnsError() {
    var repository = mock(OAuthLoginStateRepository.class);
    when(repository.findByState("state"))
        .thenReturn(
            Optional.of(
                new OAuthLoginState(
                    1L,
                    "state",
                    "http://localhost:7359/auth/google/callback",
                    Instant.now(),
                    Instant.now().plusSeconds(300))));
    var service = buildService(repository);

    var uri = service.handleCallback(null, "state", "access_denied");

    assertThat(uri.toString())
        .isEqualTo("http://localhost:7359/auth/google/callback?error=access_denied");
    verify(repository).deleteByState("state");
  }

  private GoogleOAuthService buildService(OAuthLoginStateRepository repository) {
    return new GoogleOAuthService(
        mock(AuthService.class),
        repository,
        "client-id",
        "client-secret",
        "http://localhost:8080/auth/google/callback",
        "http://localhost:*");
  }
}
