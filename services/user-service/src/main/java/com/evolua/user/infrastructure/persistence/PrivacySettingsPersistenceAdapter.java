package com.evolua.user.infrastructure.persistence;

import com.evolua.user.domain.PrivacySettings;
import com.evolua.user.domain.PrivacySettingsRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PrivacySettingsPersistenceAdapter implements PrivacySettingsRepository {
  private final PrivacySettingsJpaRepository repository;

  public PrivacySettingsPersistenceAdapter(PrivacySettingsJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public PrivacySettings save(PrivacySettings settings) {
    var entity = new PrivacySettingsEntity();
    entity.setId(settings.id());
    entity.setUserId(settings.userId());
    entity.setPrivateJournal(settings.privateJournal());
    entity.setHideSocialCheckIns(settings.hideSocialCheckIns());
    entity.setAllowHistoryInsights(settings.allowHistoryInsights());
    entity.setUseEmotionalDataForAi(settings.useEmotionalDataForAi());
    entity.setDailyReminders(settings.dailyReminders());
    entity.setContentPreferences(settings.contentPreferences());
    entity.setAiTone(settings.aiTone());
    entity.setSuggestionFrequency(settings.suggestionFrequency());
    entity.setTrailStyle(settings.trailStyle());
    entity.setUpdatedAt(settings.updatedAt());
    return map(repository.save(entity));
  }

  @Override
  public Optional<PrivacySettings> findByUserId(String userId) {
    return repository.findByUserId(userId).map(this::map);
  }

  @Override
  public void deleteByUserId(String userId) {
    repository.deleteByUserId(userId);
  }

  private PrivacySettings map(PrivacySettingsEntity entity) {
    return new PrivacySettings(
        entity.getId(),
        entity.getUserId(),
        entity.getPrivateJournal(),
        entity.getHideSocialCheckIns(),
        entity.getAllowHistoryInsights(),
        entity.getUseEmotionalDataForAi(),
        entity.getDailyReminders(),
        entity.getContentPreferences(),
        entity.getAiTone(),
        entity.getSuggestionFrequency(),
        entity.getTrailStyle(),
        entity.getUpdatedAt());
  }
}
