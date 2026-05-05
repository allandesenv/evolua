package com.evolua.user.domain;

import java.util.Optional;

public interface PrivacySettingsRepository {
  PrivacySettings save(PrivacySettings settings);

  Optional<PrivacySettings> findByUserId(String userId);

  void deleteByUserId(String userId);
}
