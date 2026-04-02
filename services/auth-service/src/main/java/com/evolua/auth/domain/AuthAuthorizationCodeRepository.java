package com.evolua.auth.domain;

import java.time.Instant;
import java.util.Optional;

public interface AuthAuthorizationCodeRepository {
  AuthAuthorizationCode save(AuthAuthorizationCode authorizationCode);

  Optional<AuthAuthorizationCode> findByCode(String code);

  void deleteByCode(String code);

  void deleteAllExpiredBefore(Instant instant);
}
