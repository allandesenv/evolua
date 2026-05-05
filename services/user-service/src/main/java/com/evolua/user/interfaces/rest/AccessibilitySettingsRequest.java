package com.evolua.user.interfaces.rest;

public record AccessibilitySettingsRequest(
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
    Boolean comfortMode) {}
