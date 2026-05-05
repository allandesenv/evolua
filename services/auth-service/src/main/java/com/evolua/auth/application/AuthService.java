package com.evolua.auth.application;

import com.evolua.auth.domain.AuthAuthorizationCode;
import com.evolua.auth.domain.AuthAuthorizationCodeRepository;
import com.evolua.auth.domain.AuthUser;
import com.evolua.auth.domain.AuthUserRepository;
import com.evolua.auth.domain.RefreshSession;
import com.evolua.auth.domain.RefreshSessionRepository;
import com.evolua.auth.infrastructure.security.TokenIssuer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private static final String LOCAL_PROVIDER = "LOCAL";
  private static final String GOOGLE_PROVIDER = "GOOGLE";
  private static final String STATUS_ACTIVE = "ACTIVE";
  private static final String STATUS_DEACTIVATED = "DEACTIVATED";
  private static final String STATUS_DELETED = "DELETED";

  private final AuthUserRepository authUserRepository;
  private final RefreshSessionRepository refreshSessionRepository;
  private final AuthAuthorizationCodeRepository authAuthorizationCodeRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenIssuer tokenIssuer;
  private final UserAccountDataClient userAccountDataClient;

  public AuthService(
      AuthUserRepository authUserRepository,
      RefreshSessionRepository refreshSessionRepository,
      AuthAuthorizationCodeRepository authAuthorizationCodeRepository,
      PasswordEncoder passwordEncoder,
      TokenIssuer tokenIssuer,
      UserAccountDataClient userAccountDataClient) {
    this.authUserRepository = authUserRepository;
    this.refreshSessionRepository = refreshSessionRepository;
    this.authAuthorizationCodeRepository = authAuthorizationCodeRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenIssuer = tokenIssuer;
    this.userAccountDataClient = userAccountDataClient;
  }

  @Transactional
  public AuthUser register(String email, String password, String displayName) {
    authUserRepository.findByEmail(email).ifPresent(existing -> {
      throw new AuthConflictException("Email ja cadastrado.");
    });

    return authUserRepository.save(
        new AuthUser(
            null,
            UUID.randomUUID().toString(),
            email,
            passwordEncoder.encode(password),
            LOCAL_PROVIDER,
            null,
            displayName,
            null,
            List.of("ROLE_USER"),
            STATUS_ACTIVE,
            Instant.now()));
  }

  @Transactional
  public AuthTokens login(String email, String password) {
    var user =
        authUserRepository
            .findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Credenciais invalidas."));

    ensureActive(user);
    if (!passwordEncoder.matches(password, user.passwordHash())) {
      throw new InvalidCredentialsException("Credenciais invalidas.");
    }

    return issueTokens(user);
  }

  @Transactional
  public AuthTokens refresh(String refreshToken) {
    var session =
        refreshSessionRepository
            .findByRefreshToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Token de sessao invalido."));
    if (session.revoked()) {
      throw new IllegalArgumentException("Token de sessao revogado.");
    }

    var user =
        authUserRepository
            .findByUserId(session.userId())
            .orElseThrow(() -> new UserNotFoundException("Usuario nao existe."));

    ensureActive(user);
    return issueTokens(user);
  }

  public AuthUser me(String userId) {
    return authUserRepository
        .findByUserId(userId)
        .orElseThrow(() -> new UserNotFoundException("Usuario nao existe."));
  }

  @Transactional
  public AuthUser findOrCreateGoogleUser(
      String providerSubject, String email, String displayName, String avatarUrl) {
    var byProvider =
        authUserRepository.findByProviderAndProviderSubject(GOOGLE_PROVIDER, providerSubject);
    if (byProvider.isPresent()) {
      ensureActive(byProvider.get());
      return syncGoogleProfile(byProvider.get(), displayName, avatarUrl);
    }

    var byEmail = authUserRepository.findByEmail(email);
    if (byEmail.isPresent()) {
      var existing = byEmail.get();
      ensureActive(existing);
      return authUserRepository.save(
          new AuthUser(
              existing.id(),
              existing.userId(),
              existing.email(),
              existing.passwordHash(),
              GOOGLE_PROVIDER,
              providerSubject,
              coalesce(displayName, existing.displayName()),
              coalesce(avatarUrl, existing.avatarUrl()),
              existing.roles(),
              existing.status(),
              existing.createdAt()));
    }

    return authUserRepository.save(
        new AuthUser(
            null,
            UUID.randomUUID().toString(),
            email,
            passwordEncoder.encode(UUID.randomUUID().toString()),
            GOOGLE_PROVIDER,
            providerSubject,
            displayName,
            avatarUrl,
            List.of("ROLE_USER"),
            STATUS_ACTIVE,
            Instant.now()));
  }

  @Transactional
  public String createAuthorizationCodeForUser(String userId) {
    authAuthorizationCodeRepository.deleteAllExpiredBefore(Instant.now());
    var code = UUID.randomUUID().toString().replace("-", "");
    authAuthorizationCodeRepository.save(
        new AuthAuthorizationCode(
            null,
            code,
            userId,
            Instant.now(),
            Instant.now().plusSeconds(120),
            false));
    return code;
  }

  @Transactional
  public AuthTokens exchangeAuthorizationCode(String code) {
    authAuthorizationCodeRepository.deleteAllExpiredBefore(Instant.now());
    var authorizationCode =
        authAuthorizationCodeRepository
            .findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Codigo de autorizacao invalido."));

    if (authorizationCode.consumed() || authorizationCode.expiresAt().isBefore(Instant.now())) {
      authAuthorizationCodeRepository.deleteByCode(code);
      throw new IllegalArgumentException("Codigo de autorizacao expirado.");
    }

    var user =
        authUserRepository
            .findByUserId(authorizationCode.userId())
            .orElseThrow(() -> new UserNotFoundException("Usuario nao existe."));

    authAuthorizationCodeRepository.deleteByCode(code);
    return issueTokens(user);
  }

  private AuthUser syncGoogleProfile(AuthUser user, String displayName, String avatarUrl) {
    if (equalsOrBlank(displayName, user.displayName()) && equalsOrBlank(avatarUrl, user.avatarUrl())) {
      return user;
    }

    return authUserRepository.save(
        new AuthUser(
            user.id(),
            user.userId(),
            user.email(),
            user.passwordHash(),
            user.provider(),
            user.providerSubject(),
            coalesce(displayName, user.displayName()),
            coalesce(avatarUrl, user.avatarUrl()),
            user.roles(),
            user.status(),
            user.createdAt()));
  }

  @Transactional
  public void changePassword(String userId, String currentPassword, String newPassword) {
    var user =
        authUserRepository
            .findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario nao existe."));
    ensureActive(user);
    if (!LOCAL_PROVIDER.equals(user.provider())) {
      throw new IllegalArgumentException(
          "Esta conta usa login com Google. Altere a senha pelo provedor.");
    }
    if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.passwordHash())) {
      throw new InvalidCredentialsException("Credenciais invalidas.");
    }
    if (newPassword == null || newPassword.length() < 6) {
      throw new IllegalArgumentException("A nova senha deve ter ao menos 6 caracteres.");
    }

    authUserRepository.save(
        new AuthUser(
            user.id(),
            user.userId(),
            user.email(),
            passwordEncoder.encode(newPassword),
            user.provider(),
            user.providerSubject(),
            user.displayName(),
            user.avatarUrl(),
            user.roles(),
            user.status(),
            user.createdAt()));
    refreshSessionRepository.revokeAll(user.userId());
  }

  @Transactional
  public void revokeSessions(String userId) {
    refreshSessionRepository.revokeAll(userId);
  }

  @Transactional
  public void deactivate(String userId, String confirmation) {
    var user =
        authUserRepository
            .findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario nao existe."));
    ensureActive(user);
    validateEmailConfirmation(user, confirmation);
    authUserRepository.save(withStatus(user, STATUS_DEACTIVATED));
    refreshSessionRepository.revokeAll(user.userId());
  }

  @Transactional
  public void deleteAccount(String userId, String confirmation, String currentPassword) {
    var user =
        authUserRepository
            .findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("Usuario nao existe."));
    if (STATUS_DELETED.equals(user.status())) {
      refreshSessionRepository.revokeAll(user.userId());
      return;
    }
    validateEmailConfirmation(user, confirmation);
    if (LOCAL_PROVIDER.equals(user.provider())
        && (currentPassword == null
            || !passwordEncoder.matches(currentPassword, user.passwordHash()))) {
      throw new InvalidCredentialsException("Credenciais invalidas.");
    }

    userAccountDataClient.deleteUserData(user.userId());
    authUserRepository.save(
        new AuthUser(
            user.id(),
            user.userId(),
            "deleted+" + user.userId() + "@deleted.evolua.local",
            passwordEncoder.encode(UUID.randomUUID().toString()),
            user.provider(),
            null,
            "Conta excluida",
            null,
            user.roles(),
            STATUS_DELETED,
            user.createdAt()));
    refreshSessionRepository.revokeAll(user.userId());
  }

  private AuthTokens issueTokens(AuthUser user) {
    refreshSessionRepository.revokeAll(user.userId());
    var access = tokenIssuer.accessToken(user.userId(), user.email(), user.roles());
    var refresh = tokenIssuer.refreshToken(user.userId(), user.email(), user.roles());
    refreshSessionRepository.save(
        new RefreshSession(null, user.userId(), refresh, Instant.now(), false));
    return new AuthTokens(access, refresh, user);
  }

  private void ensureActive(AuthUser user) {
    if (!STATUS_ACTIVE.equals(user.status())) {
      throw new InvalidCredentialsException("Credenciais invalidas.");
    }
  }

  private void validateEmailConfirmation(AuthUser user, String confirmation) {
    if (confirmation == null || !confirmation.trim().equalsIgnoreCase(user.email())) {
      throw new IllegalArgumentException("Confirmacao invalida.");
    }
  }

  private AuthUser withStatus(AuthUser user, String status) {
    return new AuthUser(
        user.id(),
        user.userId(),
        user.email(),
        user.passwordHash(),
        user.provider(),
        user.providerSubject(),
        user.displayName(),
        user.avatarUrl(),
        user.roles(),
        status,
        user.createdAt());
  }

  private boolean equalsOrBlank(String left, String right) {
    if (left == null || left.isBlank()) {
      return true;
    }
    return left.equals(right);
  }

  private String coalesce(String preferred, String fallback) {
    if (preferred != null && !preferred.isBlank()) {
      return preferred;
    }
    return fallback;
  }
}
