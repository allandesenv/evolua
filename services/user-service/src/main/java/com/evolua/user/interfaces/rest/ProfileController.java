package com.evolua.user.interfaces.rest;

import com.evolua.user.application.ProfileService;
import com.evolua.user.application.AvatarStorageService;
import com.evolua.user.application.AccessibilitySettingsService;
import com.evolua.user.application.PrivacySettingsService;
import com.evolua.user.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/profiles")
public class ProfileController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "displayName", "journeyLevel", "premium", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final ProfileService service;
  private final AvatarStorageService avatarStorageService;
  private final AccessibilitySettingsService accessibilitySettingsService;
  private final PrivacySettingsService privacySettingsService;
  private final ProfileMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public ProfileController(
      ProfileService service,
      AvatarStorageService avatarStorageService,
      AccessibilitySettingsService accessibilitySettingsService,
      PrivacySettingsService privacySettingsService,
      ProfileMapper mapper,
      CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.avatarStorageService = avatarStorageService;
    this.accessibilitySettingsService = accessibilitySettingsService;
    this.privacySettingsService = privacySettingsService;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create Profile")
  public ResponseEntity<ApiResponse<ProfileResponse>> create(@Valid @RequestBody ProfileRequest request) {
    var created =
        service.create(
            currentUserProvider.getCurrentUser().userId(),
            request.displayName(),
            request.bio(),
            request.journeyLevel(),
            request.premium());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", mapper.toResponse(created)));
  }

  @GetMapping("/me")
  @Operation(summary = "Current profile")
  public ResponseEntity<ApiResponse<ProfileResponse>> me() {
    var profile =
        service
            .findByUserId(currentUserProvider.getCurrentUser().userId())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
    return ResponseEntity.ok(ApiResponse.success(200, "Current profile", mapper.toResponse(profile)));
  }

  @PutMapping("/me")
  @Operation(summary = "Create or update current profile")
  public ResponseEntity<ApiResponse<ProfileResponse>> upsertMe(
      @Valid @RequestBody ProfileMeRequest request) {
    var profile =
        service.upsertMe(
            currentUserProvider.getCurrentUser().userId(),
            request.displayName(),
            request.bio(),
            request.journeyLevel(),
            request.birthDate(),
            request.gender(),
            request.customGender());
    return ResponseEntity.ok(ApiResponse.success(200, "Updated", mapper.toResponse(profile)));
  }

  @PostMapping(path = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload avatar for current profile")
  public ResponseEntity<ApiResponse<AvatarUploadResponse>> uploadAvatar(
      @RequestPart("file") MultipartFile file) {
    var fileName =
        avatarStorageService.store(currentUserProvider.getCurrentUser().userId(), file);
    var avatarUrl =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/v1/public/profiles/avatar/{fileName}")
            .buildAndExpand(fileName)
            .toUriString();
    var profile =
        service.updateAvatar(currentUserProvider.getCurrentUser().userId(), avatarUrl);
    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Avatar uploaded",
            new AvatarUploadResponse(profile.avatarUrl(), fileName)));
  }

  @GetMapping
  @Operation(summary = "List profiles")
  public ResponseEntity<ApiResponse<PageResponse<ProfileResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) Boolean premium) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (premium != null) {
      filters.put("premium", premium);
    }

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            premium);

    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Listed",
            PageResponse.from(
                result,
                mapper::toResponse,
                query.effectiveSortBy(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
                query.normalizedSortDir(),
                filters)));
  }

  @GetMapping("/me/privacy-settings")
  @Operation(summary = "Current profile privacy settings")
  public ResponseEntity<ApiResponse<PrivacySettingsResponse>> privacySettings() {
    var settings = privacySettingsService.get(currentUserProvider.getCurrentUser().userId());
    return ResponseEntity.ok(
        ApiResponse.success(200, "Privacy settings", PrivacySettingsResponse.from(settings)));
  }

  @GetMapping("/me/accessibility-settings")
  @Operation(summary = "Current profile accessibility settings")
  public ResponseEntity<ApiResponse<AccessibilitySettingsResponse>> accessibilitySettings() {
    var settings = accessibilitySettingsService.get(currentUserProvider.getCurrentUser().userId());
    return ResponseEntity.ok(
        ApiResponse.success(
            200, "Accessibility settings", AccessibilitySettingsResponse.from(settings)));
  }

  @PutMapping("/me/accessibility-settings")
  @Operation(summary = "Create or update current profile accessibility settings")
  public ResponseEntity<ApiResponse<AccessibilitySettingsResponse>> saveAccessibilitySettings(
      @RequestBody AccessibilitySettingsRequest request) {
    var settings =
        accessibilitySettingsService.save(
            currentUserProvider.getCurrentUser().userId(),
            request.themeMode(),
            request.highContrast(),
            request.reduceTransparency(),
            request.animationLevel(),
            request.textSize(),
            request.readingSpacing(),
            request.accessibleFont(),
            request.focusMode(),
            request.reduceMotion(),
            request.hapticFeedback(),
            request.extendedResponseTime(),
            request.simplifiedNavigation(),
            request.reduceVisualStimuli(),
            request.softerLanguage(),
            request.hideSensitiveContent(),
            request.comfortMode());
    return ResponseEntity.ok(
        ApiResponse.success(
            200, "Accessibility settings saved", AccessibilitySettingsResponse.from(settings)));
  }

  @PutMapping("/me/privacy-settings")
  @Operation(summary = "Create or update current profile privacy settings")
  public ResponseEntity<ApiResponse<PrivacySettingsResponse>> savePrivacySettings(
      @RequestBody PrivacySettingsRequest request) {
    var settings =
        privacySettingsService.save(
            currentUserProvider.getCurrentUser().userId(),
            request.privateJournal(),
            request.hideSocialCheckIns(),
            request.allowHistoryInsights(),
            request.useEmotionalDataForAi(),
            request.dailyReminders(),
            request.contentPreferences(),
            request.aiTone(),
            request.suggestionFrequency(),
            request.trailStyle());
    return ResponseEntity.ok(
        ApiResponse.success(200, "Privacy settings saved", PrivacySettingsResponse.from(settings)));
  }

  @GetMapping("/me/data-export")
  @Operation(summary = "Export current profile data")
  public ResponseEntity<ApiResponse<Map<String, Object>>> dataExport() {
    var currentUser = currentUserProvider.getCurrentUser();
    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Data export",
            privacySettingsService.dataExport(currentUser.userId(), currentUser.email())));
  }

  public record AvatarUploadResponse(String avatarUrl, String fileName) {}
}
