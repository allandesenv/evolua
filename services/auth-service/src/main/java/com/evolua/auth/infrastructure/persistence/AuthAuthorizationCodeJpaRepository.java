package com.evolua.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAuthorizationCodeJpaRepository
    extends JpaRepository<AuthAuthorizationCodeEntity, Long> {
  Optional<AuthAuthorizationCodeEntity> findByCode(String code);

  void deleteByCode(String code);

  void deleteAllByExpiresAtBefore(Instant instant);
}
