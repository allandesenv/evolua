package com.evolua.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evolua.auth.domain.AuthAuthorizationCodeRepository;
import com.evolua.auth.domain.AuthUser;
import com.evolua.auth.domain.AuthUserRepository;
import com.evolua.auth.domain.RefreshSessionRepository;
import com.evolua.auth.infrastructure.security.TokenIssuer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
  private AuthUserRepository authUserRepository;
  private RefreshSessionRepository refreshSessionRepository;
  private AuthAuthorizationCodeRepository authAuthorizationCodeRepository;
  private PasswordEncoder passwordEncoder;
  private TokenIssuer tokenIssuer;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    authUserRepository = mock(AuthUserRepository.class);
    refreshSessionRepository = mock(RefreshSessionRepository.class);
    authAuthorizationCodeRepository = mock(AuthAuthorizationCodeRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    tokenIssuer = mock(TokenIssuer.class);
    authService =
        new AuthService(
            authUserRepository,
            refreshSessionRepository,
            authAuthorizationCodeRepository,
            passwordEncoder,
            tokenIssuer);
  }

  @Test
  void loginShouldUseSafeMessageWhenUserDoesNotExist() {
    when(authUserRepository.findByEmail("ghost@evolua.app")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login("ghost@evolua.app", "123456"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Credenciais invalidas.");
  }

  @Test
  void loginShouldInformWhenPasswordIsInvalid() {
    var user =
        new AuthUser(
            1L,
            "leo-respiro",
            "leo@evolua.local",
            "encoded",
            "LOCAL",
            null,
            "Leo",
            null,
            List.of("ROLE_USER"),
            Instant.now());
    when(authUserRepository.findByEmail("leo@evolua.local")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong-pass", "encoded")).thenReturn(false);

    assertThatThrownBy(() -> authService.login("leo@evolua.local", "wrong-pass"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Credenciais invalidas.");
    verify(refreshSessionRepository, never()).revokeAll("leo-respiro");
  }
}
