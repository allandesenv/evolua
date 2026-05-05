package com.evolua.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evolua.user.domain.PrivacySettings;
import com.evolua.user.domain.PrivacySettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrivacySettingsServiceTest {
  private PrivacySettingsRepository repository;
  private ProfileService profileService;
  private AvatarStorageService avatarStorageService;
  private PrivacySettingsService service;

  @BeforeEach
  void setUp() {
    repository = mock(PrivacySettingsRepository.class);
    profileService = mock(ProfileService.class);
    avatarStorageService = mock(AvatarStorageService.class);
    service = new PrivacySettingsService(repository, profileService, avatarStorageService);
  }

  @Test
  void getShouldReturnDefaultsWhenNothingWasSaved() {
    when(repository.findByUserId("user-1")).thenReturn(Optional.empty());

    var settings = service.get("user-1");

    assertThat(settings.privateJournal()).isTrue();
    assertThat(settings.hideSocialCheckIns()).isTrue();
    assertThat(settings.aiTone()).isEqualTo("acolhedor");
    assertThat(settings.suggestionFrequency()).isEqualTo("equilibrada");
    assertThat(settings.trailStyle()).isEqualTo("guiada");
  }

  @Test
  void saveShouldValidateOptionsAndPersistValues() {
    when(repository.findByUserId("user-1")).thenReturn(Optional.empty());
    when(repository.save(org.mockito.ArgumentMatchers.any(PrivacySettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var saved =
        service.save(
            "user-1",
            false,
            false,
            true,
            true,
            false,
            true,
            "direto",
            "alta",
            "livre");

    assertThat(saved.privateJournal()).isFalse();
    assertThat(saved.hideSocialCheckIns()).isFalse();
    assertThat(saved.dailyReminders()).isFalse();
    assertThat(saved.aiTone()).isEqualTo("direto");
    assertThat(saved.suggestionFrequency()).isEqualTo("alta");
    assertThat(saved.trailStyle()).isEqualTo("livre");
  }

  @Test
  void saveShouldRejectInvalidOptions() {
    assertThatThrownBy(
            () ->
                service.save(
                    "user-1",
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    "frio",
                    "equilibrada",
                    "guiada"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Tom da IA invalido.");
  }

  @Test
  void deleteUserDataShouldRemovePreferencesProfileAndAvatar() {
    service.deleteUserData("user-1");

    verify(repository).deleteByUserId("user-1");
    verify(profileService).deleteByUserId("user-1");
    verify(avatarStorageService).deleteForUser("user-1");
  }
}
