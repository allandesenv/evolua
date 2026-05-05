package com.evolua.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
  private AuthUserRepository authUserRepository;
  private RefreshSessionRepository refreshSessionRepository;
  private AuthAuthorizationCodeRepository authAuthorizationCodeRepository;
  private PasswordEncoder passwordEncoder;
  private TokenIssuer tokenIssuer;
  private UserAccountDataClient userAccountDataClient;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    authUserRepository = mock(AuthUserRepository.class);
    refreshSessionRepository = mock(RefreshSessionRepository.class);
    authAuthorizationCodeRepository = mock(AuthAuthorizationCodeRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    tokenIssuer = mock(TokenIssuer.class);
    userAccountDataClient = mock(UserAccountDataClient.class);
    authService =
        new AuthService(
            authUserRepository,
            refreshSessionRepository,
            authAuthorizationCodeRepository,
            passwordEncoder,
            tokenIssuer,
            userAccountDataClient);
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
            "ACTIVE",
            Instant.now());
    when(authUserRepository.findByEmail("leo@evolua.local")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong-pass", "encoded")).thenReturn(false);

    assertThatThrownBy(() -> authService.login("leo@evolua.local", "wrong-pass"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Credenciais invalidas.");
    verify(refreshSessionRepository, never()).revokeAll("leo-respiro");
  }

  @Test
  void changePasswordShouldSaveEncodedPasswordAndRevokeSessions() {
    var user = localUser();
    when(authUserRepository.findByUserId("leo-respiro")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
    when(passwordEncoder.encode("654321")).thenReturn("new-encoded");

    authService.changePassword("leo-respiro", "123456", "654321");

    var captor = ArgumentCaptor.forClass(AuthUser.class);
    verify(authUserRepository).save(captor.capture());
    assertThat(captor.getValue().passwordHash()).isEqualTo("new-encoded");
    verify(refreshSessionRepository).revokeAll("leo-respiro");
  }

  @Test
  void deactivateShouldBlockFutureLoginAndRevokeSessions() {
    var user = localUser();
    when(authUserRepository.findByUserId("leo-respiro")).thenReturn(Optional.of(user));

    authService.deactivate("leo-respiro", "leo@evolua.local");

    var captor = ArgumentCaptor.forClass(AuthUser.class);
    verify(authUserRepository).save(captor.capture());
    assertThat(captor.getValue().status()).isEqualTo("DEACTIVATED");
    verify(refreshSessionRepository).revokeAll("leo-respiro");
  }

  @Test
  void deleteAccountShouldCleanupUserDataAnonymizeAndRevokeSessions() {
    var user = localUser();
    when(authUserRepository.findByUserId("leo-respiro")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
    when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("deleted-encoded");

    authService.deleteAccount("leo-respiro", "leo@evolua.local", "123456");

    var captor = ArgumentCaptor.forClass(AuthUser.class);
    verify(userAccountDataClient).deleteUserData("leo-respiro");
    verify(authUserRepository).save(captor.capture());
    assertThat(captor.getValue().status()).isEqualTo("DELETED");
    assertThat(captor.getValue().email()).isEqualTo("deleted+leo-respiro@deleted.evolua.local");
    verify(refreshSessionRepository).revokeAll("leo-respiro");
  }

  @Test
  void loginShouldRejectInactiveAccounts() {
    var inactive =
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
            "DEACTIVATED",
            Instant.now());
    when(authUserRepository.findByEmail("leo@evolua.local")).thenReturn(Optional.of(inactive));

    assertThatThrownBy(() -> authService.login("leo@evolua.local", "123456"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Credenciais invalidas.");
    verify(refreshSessionRepository, never()).revokeAll("leo-respiro");
  }

  private AuthUser localUser() {
    return new AuthUser(
        1L,
        "leo-respiro",
        "leo@evolua.local",
        "encoded",
        "LOCAL",
        null,
        "Leo",
        null,
        List.of("ROLE_USER"),
        "ACTIVE",
        Instant.now());
  }
}
