package com.evolua.auth.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserJpaRepository extends JpaRepository<AuthUserEntity, Long> {
  Optional<AuthUserEntity> findByEmail(String email);

  Optional<AuthUserEntity> findByUserId(String userId);

  Optional<AuthUserEntity> findByProviderAndProviderSubject(String provider, String providerSubject);

  void deleteByUserId(String userId);
}
