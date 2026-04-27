package com.evolua.content.interfaces.rest;

import com.evolua.content.application.TrailService;
import com.evolua.content.application.SubscriptionAccessClient;
import com.evolua.content.application.TrailJourney;
import com.evolua.content.application.TrailJourneyService;
import com.evolua.content.application.TrailJourneyStep;
import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailMediaLink;
import com.evolua.content.domain.TrailProgress;
import com.evolua.content.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/trails")
public class TrailController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "title", "category", "premium", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";
  private static final Set<String> ALLOWED_MEDIA_TYPES =
      Set.of("youtube", "video", "article", "audio", "external");

  private final TrailService service;
  private final TrailJourneyService journeyService;
  private final CurrentUserProvider currentUserProvider;
  private final SubscriptionAccessClient subscriptionAccessClient;

  public TrailController(
      TrailService service,
      TrailJourneyService journeyService,
      CurrentUserProvider currentUserProvider,
      SubscriptionAccessClient subscriptionAccessClient) {
    this.service = service;
    this.journeyService = journeyService;
    this.currentUserProvider = currentUserProvider;
    this.subscriptionAccessClient = subscriptionAccessClient;
  }

  @PostMapping
  @Operation(summary = "Create Trail")
  public ResponseEntity<ApiResponse<TrailResponse>> create(@Valid @RequestBody TrailRequest request) {
    var currentUser = currentUserProvider.getCurrentUser();
    if (!isAdmin(currentUser.roles())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can create trails");
    }

    validateRequest(request);

    var created =
        service.create(
            currentUser.userId(),
            request.title().trim(),
            request.summary().trim(),
            request.content().trim(),
            request.category().trim(),
            request.premium(),
            normalizeMediaLinks(request.mediaLinks()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", toResponse(created, true)));
  }

  @GetMapping
  @Operation(summary = "List trails")
  public ResponseEntity<ApiResponse<PageResponse<TrailResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Boolean premium) {
    var currentUser = currentUserProvider.getCurrentUser();
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (category != null && !category.isBlank()) {
      filters.put("category", category.trim());
    }
    if (premium != null) {
      filters.put("premium", premium);
    }

    var hasPremiumAccess = hasPremiumAccess(currentUser.userId(), currentUser.roles());
    var result =
        service.list(
            currentUser.userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            category == null ? null : category.trim(),
            premium);

    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Listed",
            PageResponse.from(
                result,
                trail -> toResponse(trail, hasPremiumAccess || !Boolean.TRUE.equals(trail.premium())),
                query.effectiveSortBy(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
                query.normalizedSortDir(),
                filters)));
  }

  @GetMapping("/journey/current")
  @Operation(summary = "Current private AI journey trail")
  public ResponseEntity<ApiResponse<TrailResponse>> currentJourney() {
    var currentUser = currentUserProvider.getCurrentUser();
    var trail = service.currentJourney(currentUser.userId());
    if (trail == null) {
      return ResponseEntity.ok(ApiResponse.success(200, "Current journey", null));
    }

    return ResponseEntity.ok(
        ApiResponse.success(200, "Current journey", toResponse(trail, true)));
  }

  @GetMapping("/{trailId}/journey")
  @Operation(summary = "Trail visual journey and progress")
  public ResponseEntity<ApiResponse<TrailJourneyResponse>> journey(@PathVariable Long trailId) {
    var currentUser = currentUserProvider.getCurrentUser();
    var journey = journeyService.getJourney(currentUser.userId(), trailId);
    ensureJourneyAccessible(journey, currentUser.userId(), currentUser.roles());
    return ResponseEntity.ok(
        ApiResponse.success(200, "Trail journey", toJourneyResponse(journey)));
  }

  @PostMapping("/{trailId}/journey/start")
  @Operation(summary = "Start trail journey")
  public ResponseEntity<ApiResponse<TrailJourneyResponse>> startJourney(@PathVariable Long trailId) {
    var currentUser = currentUserProvider.getCurrentUser();
    ensureTrailAccessible(service.findById(trailId), currentUser.userId(), currentUser.roles());
    var journey = journeyService.startJourney(currentUser.userId(), trailId);
    return ResponseEntity.ok(
        ApiResponse.success(200, "Trail journey started", toJourneyResponse(journey)));
  }

  @PostMapping("/{trailId}/journey/steps/{stepIndex}/complete")
  @Operation(summary = "Complete trail journey step")
  public ResponseEntity<ApiResponse<TrailJourneyResponse>> completeJourneyStep(
      @PathVariable Long trailId, @PathVariable Integer stepIndex) {
    var currentUser = currentUserProvider.getCurrentUser();
    ensureTrailAccessible(service.findById(trailId), currentUser.userId(), currentUser.roles());
    var journey = journeyService.completeStep(currentUser.userId(), trailId, stepIndex);
    return ResponseEntity.ok(
        ApiResponse.success(200, "Trail journey step completed", toJourneyResponse(journey)));
  }

  @PostMapping("/internal/journey/current")
  @Operation(summary = "Upsert current private AI journey trail")
  public ResponseEntity<ApiResponse<TrailResponse>> upsertCurrentJourney(
      @Valid @RequestBody JourneyTrailRequest request) {
    var currentUser = currentUserProvider.getCurrentUser();
    var saved =
        service.upsertJourneyTrail(
            currentUser.userId(),
            request.title().trim(),
            request.summary().trim(),
            request.content().trim(),
            request.category().trim(),
            normalizeMediaLinks(request.mediaLinks()),
            request.journeyKey().trim(),
            request.sourceStyle() == null ? null : request.sourceStyle().trim());
    return ResponseEntity.ok(
        ApiResponse.success(200, "Current journey updated", toResponse(saved, true)));
  }

  private void validateRequest(TrailRequest request) {
    if (request.content().trim().isEmpty()) {
      throw new IllegalArgumentException("content must not be blank");
    }

    for (var mediaLink : request.mediaLinks() == null ? List.<TrailMediaLinkRequest>of() : request.mediaLinks()) {
      validateMediaLink(mediaLink);
    }
  }

  private void validateMediaLink(TrailMediaLinkRequest mediaLink) {
    var normalizedType = mediaLink.type().trim().toLowerCase(Locale.ROOT);
    if (!ALLOWED_MEDIA_TYPES.contains(normalizedType)) {
      throw new IllegalArgumentException("mediaLinks.type must be one of: youtube, video, article, audio, external");
    }

    URI uri;
    try {
      uri = URI.create(mediaLink.url().trim());
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("mediaLinks.url must be a valid URL");
    }

    var scheme = uri.getScheme();
    if (scheme == null || (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http"))) {
      throw new IllegalArgumentException("mediaLinks.url must use http or https");
    }

    var host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
    if ("youtube".equals(normalizedType) && !(host.contains("youtube.com") || host.contains("youtu.be"))) {
      throw new IllegalArgumentException("youtube links must use youtube.com or youtu.be");
    }
  }

  private List<TrailMediaLink> normalizeMediaLinks(List<TrailMediaLinkRequest> mediaLinks) {
    if (mediaLinks == null || mediaLinks.isEmpty()) {
      return List.of();
    }

    return mediaLinks.stream()
        .map(
            item ->
                new TrailMediaLink(
                    item.label().trim(),
                    item.url().trim(),
                    item.type().trim().toLowerCase(Locale.ROOT)))
        .toList();
  }

  private TrailResponse toResponse(Trail item, boolean accessible) {
    return new TrailResponse(
        item.id(),
        item.userId(),
        item.title(),
        item.summary(),
        accessible ? item.content() : null,
        item.category(),
        item.premium(),
        item.privateTrail(),
        item.activeJourney(),
        item.generatedByAi(),
        item.journeyKey(),
        item.sourceStyle(),
        accessible,
        accessible
            ? item.mediaLinks().stream()
                .map(link -> new TrailMediaLinkResponse(link.label(), link.url(), link.type()))
                .toList()
            : List.of(),
        item.createdAt());
  }

  private TrailJourneyResponse toJourneyResponse(TrailJourney journey) {
    return new TrailJourneyResponse(
        toResponse(journey.trail(), true),
        journey.steps().stream().map(this::toStepResponse).toList(),
        toProgressResponse(journey.progress()),
        journey.progressPercent(),
        journey.nextStep() == null ? null : toStepResponse(journey.nextStep()));
  }

  private TrailJourneyStepResponse toStepResponse(TrailJourneyStep step) {
    return new TrailJourneyStepResponse(
        step.index(),
        step.title(),
        step.summary(),
        step.content(),
        step.status(),
        step.estimatedMinutes(),
        step.mediaLinks().stream()
            .map(link -> new TrailMediaLinkResponse(link.label(), link.url(), link.type()))
            .toList());
  }

  private TrailProgressResponse toProgressResponse(TrailProgress progress) {
    if (progress == null) {
      return null;
    }
    return new TrailProgressResponse(
        progress.currentStepIndex(),
        progress.completedStepIndexes(),
        progress.startedAt(),
        progress.updatedAt(),
        progress.completedAt());
  }

  private boolean isAdmin(List<String> roles) {
    return roles.contains("ROLE_ADMIN");
  }

  private boolean hasPremiumAccess(String userId, List<String> roles) {
    return isAdmin(roles) || roles.contains("ROLE_PREMIUM") || subscriptionAccessClient.hasPremiumAccess(userId);
  }

  private void ensureJourneyAccessible(TrailJourney journey, String userId, List<String> roles) {
    ensureTrailAccessible(journey.trail(), userId, roles);
  }

  private void ensureTrailAccessible(Trail trail, String userId, List<String> roles) {
    if (trail == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada.");
    }
    if (Boolean.TRUE.equals(trail.privateTrail()) && !userId.equals(trail.userId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada.");
    }
    if (Boolean.TRUE.equals(trail.premium()) && !hasPremiumAccess(userId, roles)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Premium access required");
    }
  }

  public record JourneyTrailRequest(
      @jakarta.validation.constraints.NotBlank String title,
      @jakarta.validation.constraints.NotBlank String summary,
      @jakarta.validation.constraints.NotBlank String content,
      @jakarta.validation.constraints.NotBlank String category,
      @jakarta.validation.constraints.NotBlank String journeyKey,
      String sourceStyle,
      List<@Valid TrailMediaLinkRequest> mediaLinks) {}
}
