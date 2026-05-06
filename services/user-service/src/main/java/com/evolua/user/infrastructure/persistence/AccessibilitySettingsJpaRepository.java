package com.evolua.user.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessibilitySettingsJpaRepository
    extends JpaRepository<AccessibilitySettingsEntity, Long> {
  Optional<AccessibilitySettingsEntity> findByUserId(String userId);

  void deleteByUserId(String userId);
}
