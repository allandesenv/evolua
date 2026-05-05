package com.evolua.user.domain;

import java.time.Instant;

public record AccessibilitySettings(
    Long id,
    String userId,
    String themeMode,
    Boolean highContrast,
    Boolean reduceTransparency,
    String animationLevel,
    String textSize,
    String readingSpacing,
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
    Instant updatedAt) {
  public static AccessibilitySettings defaults(String userId) {
    return new AccessibilitySettings(
        null,
        userId,
        "dark",
        false,
        false,
        "normal",
        "normal",
        "comfortable",
        false,
        false,
        false,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        Instant.now());
  }
}
