package com.evolua.user.interfaces.rest;

import com.evolua.user.domain.AccessibilitySettings;
import java.time.Instant;

public record AccessibilitySettingsResponse(
    Boolean highContrast,
    Boolean reduceTransparency,
    Boolean accessibleFont,
    Boolean focusMode,
    Boolean reduceMotion,
    Boolean hapticFeedback,
    Boolean extendedResponseTime,
    Boolean simplifiedNavigation,
    Boolean reduceVisualStimuli,
    Boolean softerLanguage,
    Boolean hideSensitiveContent,
    Boolean comfortMode,
    String themeMode,
    String animationLevel,
    String textSize,
    String readingSpacing,
    Instant updatedAt) {
  public static AccessibilitySettingsResponse from(AccessibilitySettings settings) {
    return new AccessibilitySettingsResponse(
        settings.highContrast(),
        settings.reduceTransparency(),
        settings.accessibleFont(),
        settings.focusMode(),
        settings.reduceMotion(),
        settings.hapticFeedback(),
        settings.extendedResponseTime(),
        settings.simplifiedNavigation(),
        settings.reduceVisualStimuli(),
        settings.softerLanguage(),
        settings.hideSensitiveContent(),
        settings.comfortMode(),
        settings.themeMode(),
        settings.animationLevel(),
        settings.textSize(),
        settings.readingSpacing(),
        settings.updatedAt());
  }
}
