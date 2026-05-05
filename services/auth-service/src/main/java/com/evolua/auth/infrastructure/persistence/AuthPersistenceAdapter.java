package com.evolua.auth.infrastructure.persistence;

import com.evolua.auth.domain.AuthAuthorizationCode;
import com.evolua.auth.domain.AuthAuthorizationCodeRepository;
import com.evolua.auth.domain.AuthUser;
import com.evolua.auth.domain.AuthUserRepository;
import com.evolua.auth.domain.OAuthLoginState;
import com.evolua.auth.domain.OAuthLoginStateRepository;
import com.evolua.auth.domain.RefreshSession;
import com.evolua.auth.domain.RefreshSessionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AuthPersistenceAdapter
    implements AuthUserRepository,
        RefreshSessionRepository,
        OAuthLoginStateRepository,
        AuthAuthorizationCodeRepository {
  private final AuthUserJpaRepository authUserJpaRepository;
  private final RefreshSessionJpaRepository refreshSessionJpaRepository;
  private final OAuthLoginStateJpaRepository oAuthLoginStateJpaRepository;
  private final AuthAuthorizationCodeJpaRepository authAuthorizationCodeJpaRepository;

  public AuthPersistenceAdapter(
      AuthUserJpaRepository authUserJpaRepository,
      RefreshSessionJpaRepository refreshSessionJpaRepository,
      OAuthLoginStateJpaRepository oAuthLoginStateJpaRepository,
      AuthAuthorizationCodeJpaRepository authAuthorizationCodeJpaRepository) {
    this.authUserJpaRepository = authUserJpaRepository;
    this.refreshSessionJpaRepository = refreshSessionJpaRepository;
    this.oAuthLoginStateJpaRepository = oAuthLoginStateJpaRepository;
    this.authAuthorizationCodeJpaRepository = authAuthorizationCodeJpaRepository;
  }

  @Override
  public AuthUser save(AuthUser user) {
    var entity = new AuthUserEntity();
    entity.setId(user.id());
    entity.setUserId(user.userId());
    entity.setEmail(user.email());
    entity.setPasswordHash(user.passwordHash());
    entity.setProvider(user.provider());
    entity.setProviderSubject(user.providerSubject());
    entity.setDisplayName(user.displayName());
    entity.setAvatarUrl(user.avatarUrl());
    entity.setRoles(String.join(",", user.roles()));
    entity.setStatus(user.status());
    entity.setCreatedAt(user.createdAt());
    return map(authUserJpaRepository.save(entity));
  }

  @Override
  public Optional<AuthUser> findByEmail(String email) {
    return authUserJpaRepository.findByEmail(email).map(this::map);
  }

  @Override
  public Optional<AuthUser> findByUserId(String userId) {
    return authUserJpaRepository.findByUserId(userId).map(this::map);
  }

  @Override
  public Optional<AuthUser> findByProviderAndProviderSubject(String provider, String providerSubject) {
    return authUserJpaRepository.findByProviderAndProviderSubject(provider, providerSubject).map(this::map);
  }

  @Override
  public void deleteByUserId(String userId) {
    authUserJpaRepository.deleteByUserId(userId);
  }

  @Override
  public RefreshSession save(RefreshSession session) {
    var entity = new RefreshSessionEntity();
    entity.setId(session.id());
    entity.setUserId(session.userId());
    entity.setRefreshToken(session.refreshToken());
    entity.setCreatedAt(session.createdAt());
    entity.setRevoked(session.revoked());
    return map(refreshSessionJpaRepository.save(entity));
  }

  @Override
  public Optional<RefreshSession> findByRefreshToken(String refreshToken) {
    return refreshSessionJpaRepository.findByRefreshToken(refreshToken).map(this::map);
  }

  @Override
  public void revokeAll(String userId) {
    for (var session : refreshSessionJpaRepository.findAllByUserId(userId)) {
      session.setRevoked(true);
      refreshSessionJpaRepository.save(session);
    }
  }

  @Override
  public OAuthLoginState save(OAuthLoginState state) {
    var entity = new OAuthLoginStateEntity();
    entity.setId(state.id());
    entity.setState(state.state());
    entity.setFrontendRedirectUri(state.frontendRedirectUri());
    entity.setCreatedAt(state.createdAt());
    entity.setExpiresAt(state.expiresAt());
    return map(oAuthLoginStateJpaRepository.save(entity));
  }

  @Override
  public Optional<OAuthLoginState> findByState(String state) {
    return oAuthLoginStateJpaRepository.findByState(state).map(this::map);
  }

  @Override
  public void deleteByState(String state) {
    oAuthLoginStateJpaRepository.deleteByState(state);
  }

  @Override
  public void deleteAllExpiredBefore(Instant instant) {
    oAuthLoginStateJpaRepository.deleteAllByExpiresAtBefore(instant);
    authAuthorizationCodeJpaRepository.deleteAllByExpiresAtBefore(instant);
  }

  @Override
  public AuthAuthorizationCode save(AuthAuthorizationCode authorizationCode) {
    var entity = new AuthAuthorizationCodeEntity();
    entity.setId(authorizationCode.id());
    entity.setCode(authorizationCode.code());
    entity.setUserId(authorizationCode.userId());
    entity.setCreatedAt(authorizationCode.createdAt());
    entity.setExpiresAt(authorizationCode.expiresAt());
    entity.setConsumed(authorizationCode.consumed());
    return map(authAuthorizationCodeJpaRepository.save(entity));
  }

  @Override
  public Optional<AuthAuthorizationCode> findByCode(String code) {
    return authAuthorizationCodeJpaRepository.findByCode(code).map(this::map);
  }

  @Override
  public void deleteByCode(String code) {
    authAuthorizationCodeJpaRepository.deleteByCode(code);
  }

  private AuthUser map(AuthUserEntity saved) {
    return new AuthUser(
        saved.getId(),
        saved.getUserId(),
        saved.getEmail(),
        saved.getPasswordHash(),
        saved.getProvider(),
        saved.getProviderSubject(),
        saved.getDisplayName(),
        saved.getAvatarUrl(),
        List.of(saved.getRoles().split(",")),
        saved.getStatus(),
        saved.getCreatedAt());
  }

  private RefreshSession map(RefreshSessionEntity saved) {
    return new RefreshSession(
        saved.getId(),
        saved.getUserId(),
        saved.getRefreshToken(),
        saved.getCreatedAt(),
        saved.isRevoked());
  }

  private OAuthLoginState map(OAuthLoginStateEntity saved) {
    return new OAuthLoginState(
        saved.getId(),
        saved.getState(),
        saved.getFrontendRedirectUri(),
        saved.getCreatedAt(),
        saved.getExpiresAt());
  }

  private AuthAuthorizationCode map(AuthAuthorizationCodeEntity saved) {
    return new AuthAuthorizationCode(
        saved.getId(),
        saved.getCode(),
        saved.getUserId(),
        saved.getCreatedAt(),
        saved.getExpiresAt(),
        saved.isConsumed());
  }
}
