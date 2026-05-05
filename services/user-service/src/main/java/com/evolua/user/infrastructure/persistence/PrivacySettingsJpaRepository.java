package com.evolua.user.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivacySettingsJpaRepository extends JpaRepository<PrivacySettingsEntity, Long> {
  Optional<PrivacySettingsEntity> findByUserId(String userId);

  void deleteByUserId(String userId);
}
