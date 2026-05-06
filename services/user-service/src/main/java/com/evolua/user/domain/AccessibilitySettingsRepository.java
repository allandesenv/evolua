package com.evolua.user.domain;

import java.util.Optional;

public interface AccessibilitySettingsRepository {
  AccessibilitySettings save(AccessibilitySettings settings);

  Optional<AccessibilitySettings> findByUserId(String userId);

  void deleteByUserId(String userId);
}
