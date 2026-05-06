package com.evolua.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evolua.user.domain.AccessibilitySettings;
import com.evolua.user.domain.AccessibilitySettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessibilitySettingsServiceTest {
  private AccessibilitySettingsRepository repository;
  private AccessibilitySettingsService service;

  @BeforeEach
  void setUp() {
    repository = mock(AccessibilitySettingsRepository.class);
    service = new AccessibilitySettingsService(repository);
  }

  @Test
  void getShouldReturnDefaultsWhenNothingWasSaved() {
    when(repository.findByUserId("user-1")).thenReturn(Optional.empty());

    var settings = service.get("user-1");

    assertThat(settings.themeMode()).isEqualTo("dark");
    assertThat(settings.highContrast()).isFalse();
    assertThat(settings.textSize()).isEqualTo("normal");
    assertThat(settings.readingSpacing()).isEqualTo("comfortable");
    assertThat(settings.hapticFeedback()).isTrue();
    assertThat(settings.comfortMode()).isFalse();
  }

  @Test
  void saveShouldValidateOptionsAndPersistValues() {
    when(repository.findByUserId("user-1")).thenReturn(Optional.empty());
    when(repository.save(org.mockito.ArgumentMatchers.any(AccessibilitySettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var saved =
        service.save(
            "user-1",
            "light",
            true,
            true,
            "reduced",
            "large",
            "wide",
            true,
            true,
            true,
            false,
            true,
            true,
            true,
            true,
            true,
            true);

    assertThat(saved.themeMode()).isEqualTo("light");
    assertThat(saved.highContrast()).isTrue();
    assertThat(saved.animationLevel()).isEqualTo("reduced");
    assertThat(saved.textSize()).isEqualTo("large");
    assertThat(saved.readingSpacing()).isEqualTo("wide");
    assertThat(saved.hapticFeedback()).isFalse();
    assertThat(saved.comfortMode()).isTrue();
  }

  @Test
  void saveShouldRejectInvalidOptions() {
    assertThatThrownBy(
            () ->
                service.save(
                    "user-1",
                    "sepia",
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
                    false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Tema invalido.");
  }

  @Test
  void deleteByUserIdShouldRemovePreferences() {
    service.deleteByUserId("user-1");

    verify(repository).deleteByUserId("user-1");
  }
}
