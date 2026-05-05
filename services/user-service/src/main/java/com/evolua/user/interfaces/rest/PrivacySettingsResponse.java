package com.evolua.user.interfaces.rest;

import com.evolua.user.domain.PrivacySettings;
import java.time.Instant;

public record PrivacySettingsResponse(
    Boolean privateJournal,
    Boolean hideSocialCheckIns,
    Boolean allowHistoryInsights,
    Boolean useEmotionalDataForAi,
    Boolean dailyReminders,
    Boolean contentPreferences,
    String aiTone,
    String suggestionFrequency,
    String trailStyle,
    Instant updatedAt) {
  static PrivacySettingsResponse from(PrivacySettings settings) {
    return new PrivacySettingsResponse(
        settings.privateJournal(),
        settings.hideSocialCheckIns(),
        settings.allowHistoryInsights(),
        settings.useEmotionalDataForAi(),
        settings.dailyReminders(),
        settings.contentPreferences(),
        settings.aiTone(),
        settings.suggestionFrequency(),
        settings.trailStyle(),
        settings.updatedAt());
  }
}
