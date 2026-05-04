package com.evolua.user.interfaces.rest;

import com.evolua.user.application.ProfileService;
import com.evolua.user.application.AvatarStorageService;
import com.evolua.user.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
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
  private final ProfileMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public ProfileController(
      ProfileService service,
      AvatarStorageService avatarStorageService,
      ProfileMapper mapper,
      CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.avatarStorageService = avatarStorageService;
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

  public record AvatarUploadResponse(String avatarUrl, String fileName) {}
}
