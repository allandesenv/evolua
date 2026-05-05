package com.evolua.user.application;

import com.evolua.user.domain.PrivacySettings;
import com.evolua.user.domain.PrivacySettingsRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivacySettingsService {
  private static final Set<String> AI_TONES = Set.of("acolhedor", "direto", "reflexivo");
  private static final Set<String> SUGGESTION_FREQUENCIES = Set.of("baixa", "equilibrada", "alta");
  private static final Set<String> TRAIL_STYLES = Set.of("guiada", "livre", "profunda");

  private final PrivacySettingsRepository repository;
  private final ProfileService profileService;
  private final AvatarStorageService avatarStorageService;
  private final AccessibilitySettingsService accessibilitySettingsService;
  private final FeedbackService feedbackService;

  public PrivacySettingsService(
      PrivacySettingsRepository repository,
      ProfileService profileService,
      AvatarStorageService avatarStorageService,
      AccessibilitySettingsService accessibilitySettingsService,
      FeedbackService feedbackService) {
    this.repository = repository;
    this.profileService = profileService;
    this.avatarStorageService = avatarStorageService;
    this.accessibilitySettingsService = accessibilitySettingsService;
    this.feedbackService = feedbackService;
  }

  public PrivacySettings get(String userId) {
    return repository.findByUserId(userId).orElseGet(() -> PrivacySettings.defaults(userId));
  }

  @Transactional
  public PrivacySettings save(
      String userId,
      Boolean privateJournal,
      Boolean hideSocialCheckIns,
      Boolean allowHistoryInsights,
      Boolean useEmotionalDataForAi,
      Boolean dailyReminders,
      Boolean contentPreferences,
      String aiTone,
      String suggestionFrequency,
      String trailStyle) {
    var existing = repository.findByUserId(userId).orElse(null);
    return repository.save(
        new PrivacySettings(
            existing == null ? null : existing.id(),
            userId,
            bool(privateJournal, true),
            bool(hideSocialCheckIns, true),
            bool(allowHistoryInsights, true),
            bool(useEmotionalDataForAi, true),
            bool(dailyReminders, true),
            bool(contentPreferences, true),
            option(aiTone, AI_TONES, "acolhedor", "Tom da IA invalido."),
            option(
                suggestionFrequency,
                SUGGESTION_FREQUENCIES,
                "equilibrada",
                "Frequencia de sugestoes invalida."),
            option(trailStyle, TRAIL_STYLES, "guiada", "Estilo das trilhas invalido."),
            Instant.now()));
  }

  public Map<String, Object> dataExport(String userId, String email) {
    var export = new LinkedHashMap<String, Object>();
    export.put("email", email);
    export.put("profile", profileService.findByUserId(userId).map(ProfileExport::from).orElse(null));
    export.put("preferences", get(userId));
    export.put("accessibilityPreferences", accessibilitySettingsService.get(userId));
    export.put("feedbackSubmissions", feedbackService.findByUserId(userId).stream().map(FeedbackExport::from).toList());
    export.put("exportedAt", Instant.now().toString());
    export.put("message", "Exportacao gerada pelo backend Evolua.");
    return export;
  }

  @Transactional
  public void deleteUserData(String userId) {
    repository.deleteByUserId(userId);
    accessibilitySettingsService.deleteByUserId(userId);
    feedbackService.deleteByUserId(userId);
    profileService.deleteByUserId(userId);
    avatarStorageService.deleteForUser(userId);
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

  private record ProfileExport(
      String displayName,
      String bio,
      Integer journeyLevel,
      Boolean premium,
      String birthDate,
      String gender,
      String customGender,
      String avatarUrl,
      String createdAt) {
    static ProfileExport from(com.evolua.user.domain.Profile profile) {
      return new ProfileExport(
          profile.displayName(),
          profile.bio(),
          profile.journeyLevel(),
          profile.premium(),
          profile.birthDate() == null ? null : profile.birthDate().toString(),
          profile.gender(),
          profile.customGender(),
          profile.avatarUrl(),
          profile.createdAt().toString());
    }
  }

  private record FeedbackExport(
      String workingWell,
      String couldImprove,
      String confusingOrHard,
      String helpedHow,
      String featureSuggestion,
      String contentSuggestion,
      String visualSuggestion,
      String aiSuggestion,
      String problemWhatHappened,
      String problemWhere,
      String problemCanRepeat,
      String rating,
      String ratingComment,
      Boolean screenshotAttached,
      String status,
      String createdAt) {
    static FeedbackExport from(com.evolua.user.domain.FeedbackSubmission feedback) {
      return new FeedbackExport(
          feedback.workingWell(),
          feedback.couldImprove(),
          feedback.confusingOrHard(),
          feedback.helpedHow(),
          feedback.featureSuggestion(),
          feedback.contentSuggestion(),
          feedback.visualSuggestion(),
          feedback.aiSuggestion(),
          feedback.problemWhatHappened(),
          feedback.problemWhere(),
          feedback.problemCanRepeat(),
          feedback.rating(),
          feedback.ratingComment(),
          feedback.screenshotFileName() != null && !feedback.screenshotFileName().isBlank(),
          feedback.status(),
          feedback.createdAt().toString());
    }
  }
}
