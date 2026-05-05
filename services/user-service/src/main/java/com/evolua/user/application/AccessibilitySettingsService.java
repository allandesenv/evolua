package com.evolua.user.application;

import com.evolua.user.domain.AccessibilitySettings;
import com.evolua.user.domain.AccessibilitySettingsRepository;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessibilitySettingsService {
  private static final Set<String> THEME_MODES = Set.of("dark", "light", "system");
  private static final Set<String> ANIMATION_LEVELS = Set.of("normal", "reduced", "none");
  private static final Set<String> TEXT_SIZES = Set.of("small", "normal", "large", "extraLarge");
  private static final Set<String> READING_SPACINGS = Set.of("compact", "comfortable", "wide");

  private final AccessibilitySettingsRepository repository;

  public AccessibilitySettingsService(AccessibilitySettingsRepository repository) {
    this.repository = repository;
  }

  public AccessibilitySettings get(String userId) {
    return repository.findByUserId(userId).orElseGet(() -> AccessibilitySettings.defaults(userId));
  }

  @Transactional
  public AccessibilitySettings save(
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
      Boolean comfortMode) {
    var existing = repository.findByUserId(userId).orElse(null);
    return repository.save(
        new AccessibilitySettings(
            existing == null ? null : existing.id(),
            userId,
            option(themeMode, THEME_MODES, "dark", "Tema invalido."),
            bool(highContrast, false),
            bool(reduceTransparency, false),
            option(animationLevel, ANIMATION_LEVELS, "normal", "Nivel de animacao invalido."),
            option(textSize, TEXT_SIZES, "normal", "Tamanho de texto invalido."),
            option(readingSpacing, READING_SPACINGS, "comfortable", "Espacamento de leitura invalido."),
            bool(accessibleFont, false),
            bool(focusMode, false),
            bool(reduceMotion, false),
            bool(hapticFeedback, true),
            bool(extendedResponseTime, false),
            bool(simplifiedNavigation, false),
            bool(reduceVisualStimuli, false),
            bool(softerLanguage, false),
            bool(hideSensitiveContent, false),
            bool(comfortMode, false),
            Instant.now()));
  }

  @Transactional
  public void deleteByUserId(String userId) {
    repository.deleteByUserId(userId);
  }

  private boolean bool(Boolean value, boolean fallback) {
    return value == null ? fallback : value;
  }

  private String option(String value, Set<String> allowed, String fallback, String message) {
    var normalized = value == null || value.isBlank() ? fallback : value.trim();
    if (!allowed.contains(normalized)) {
      throw new IllegalArgumentException(message);
    }
    return normalized;
  }
}
