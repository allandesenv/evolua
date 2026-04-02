package com.evolua.content.interfaces.rest;

import com.evolua.content.application.TrailService;
import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailMediaLink;
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
  private final CurrentUserProvider currentUserProvider;

  public TrailController(TrailService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
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

    var hasPremiumAccess = hasPremiumAccess(currentUser.roles());
    var result =
        service.list(
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
        accessible,
        accessible
            ? item.mediaLinks().stream()
                .map(link -> new TrailMediaLinkResponse(link.label(), link.url(), link.type()))
                .toList()
            : List.of(),
        item.createdAt());
  }

  private boolean isAdmin(List<String> roles) {
    return roles.contains("ROLE_ADMIN");
  }

  private boolean hasPremiumAccess(List<String> roles) {
    return isAdmin(roles) || roles.contains("ROLE_PREMIUM");
  }
}
