package com.evolua.user.infrastructure.persistence;

import com.evolua.user.domain.AccessibilitySettings;
import com.evolua.user.domain.AccessibilitySettingsRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AccessibilitySettingsPersistenceAdapter implements AccessibilitySettingsRepository {
  private final AccessibilitySettingsJpaRepository repository;

  public AccessibilitySettingsPersistenceAdapter(AccessibilitySettingsJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public AccessibilitySettings save(AccessibilitySettings settings) {
    var entity = new AccessibilitySettingsEntity();
    entity.setId(settings.id());
    entity.setUserId(settings.userId());
    entity.setThemeMode(settings.themeMode());
    entity.setHighContrast(settings.highContrast());
    entity.setReduceTransparency(settings.reduceTransparency());
    entity.setAnimationLevel(settings.animationLevel());
    entity.setTextSize(settings.textSize());
    entity.setReadingSpacing(settings.readingSpacing());
    entity.setAccessibleFont(settings.accessibleFont());
    entity.setFocusMode(settings.focusMode());
    entity.setReduceMotion(settings.reduceMotion());
    entity.setHapticFeedback(settings.hapticFeedback());
    entity.setExtendedResponseTime(settings.extendedResponseTime());
    entity.setSimplifiedNavigation(settings.simplifiedNavigation());
    entity.setReduceVisualStimuli(settings.reduceVisualStimuli());
    entity.setSofterLanguage(settings.softerLanguage());
    entity.setHideSensitiveContent(settings.hideSensitiveContent());
    entity.setComfortMode(settings.comfortMode());
    entity.setUpdatedAt(settings.updatedAt());
    return map(repository.save(entity));
  }

  @Override
  public Optional<AccessibilitySettings> findByUserId(String userId) {
    return repository.findByUserId(userId).map(this::map);
  }

  @Override
  public void deleteByUserId(String userId) {
    repository.deleteByUserId(userId);
  }

  private AccessibilitySettings map(AccessibilitySettingsEntity entity) {
    return new AccessibilitySettings(
        entity.getId(),
        entity.getUserId(),
        entity.getThemeMode(),
        entity.getHighContrast(),
        entity.getReduceTransparency(),
        entity.getAnimationLevel(),
        entity.getTextSize(),
        entity.getReadingSpacing(),
        entity.getAccessibleFont(),
        entity.getFocusMode(),
        entity.getReduceMotion(),
        entity.getHapticFeedback(),
        entity.getExtendedResponseTime(),
        entity.getSimplifiedNavigation(),
        entity.getReduceVisualStimuli(),
        entity.getSofterLanguage(),
        entity.getHideSensitiveContent(),
        entity.getComfortMode(),
        entity.getUpdatedAt());
  }
}
