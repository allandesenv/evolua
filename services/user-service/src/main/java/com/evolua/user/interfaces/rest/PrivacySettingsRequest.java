package com.evolua.user.interfaces.rest;

public record PrivacySettingsRequest(
    Boolean privateJournal,
    Boolean hideSocialCheckIns,
    Boolean allowHistoryInsights,
    Boolean useEmotionalDataForAi,
    Boolean dailyReminders,
    Boolean contentPreferences,
    String aiTone,
    String suggestionFrequency,
    String trailStyle) {}
