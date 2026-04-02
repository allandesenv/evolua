package com.evolua.auth.domain;

import java.time.Instant;
import java.util.Optional;

public interface OAuthLoginStateRepository {
  OAuthLoginState save(OAuthLoginState state);

  Optional<OAuthLoginState> findByState(String state);

  void deleteByState(String state);

  void deleteAllExpiredBefore(Instant instant);
}
