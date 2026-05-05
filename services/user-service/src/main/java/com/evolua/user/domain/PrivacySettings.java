package com.evolua.user.domain;

import java.time.Instant;

public record PrivacySettings(
    Long id,
    String userId,
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
  public static PrivacySettings defaults(String userId) {
    return new PrivacySettings(
        null,
        userId,
        true,
        true,
        true,
        true,
        true,
        true,
        "acolhedor",
        "equilibrada",
        "guiada",
        Instant.now());
  }
}
