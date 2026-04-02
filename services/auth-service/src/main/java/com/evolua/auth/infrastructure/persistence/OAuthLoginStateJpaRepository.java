package com.evolua.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthLoginStateJpaRepository extends JpaRepository<OAuthLoginStateEntity, Long> {
  Optional<OAuthLoginStateEntity> findByState(String state);

  void deleteByState(String state);

  void deleteAllByExpiresAtBefore(Instant instant);
}
