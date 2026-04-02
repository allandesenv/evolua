package com.evolua.social.interfaces.rest;

import com.evolua.social.application.CommunityService;
import com.evolua.social.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/communities")
public class CommunityController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "slug", "name", "visibility", "category", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final CommunityService service;
  private final CommunityMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public CommunityController(
      CommunityService service, CommunityMapper mapper, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create community")
  public ResponseEntity<ApiResponse<CommunityResponse>> create(@Valid @RequestBody CommunityRequest request) {
    String userId = currentUserProvider.getCurrentUser().userId();
    var created =
        service.create(
            userId,
            request.slug(),
            request.name(),
            request.description(),
            request.visibility(),
            request.category());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", mapper.toResponse(created, userId)));
  }

  @GetMapping
  @Operation(summary = "List communities")
  public ResponseEntity<ApiResponse<PageResponse<CommunityResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String visibility,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Boolean joined) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (visibility != null && !visibility.isBlank()) {
      filters.put("visibility", visibility.trim().toUpperCase());
    }
    if (category != null && !category.isBlank()) {
      filters.put("category", category.trim().toLowerCase());
    }
    if (joined != null) {
      filters.put("joined", joined);
    }

    String userId = currentUserProvider.getCurrentUser().userId();
    var result =
        service.list(
            userId,
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            visibility,
            category,
            joined);

    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Listed",
            PageResponse.from(
                result,
                item -> mapper.toResponse(item, userId),
                query.effectiveSortBy(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
                query.normalizedSortDir(),
                filters)));
  }

  @PostMapping("/{id}/join")
  @Operation(summary = "Join community")
  public ResponseEntity<ApiResponse<CommunityResponse>> join(@PathVariable String id) {
    String userId = currentUserProvider.getCurrentUser().userId();
    var updated = service.join(userId, id);
    return ResponseEntity.ok(ApiResponse.success(200, "Joined", mapper.toResponse(updated, userId)));
  }

  @PostMapping("/{id}/leave")
  @Operation(summary = "Leave community")
  public ResponseEntity<ApiResponse<CommunityResponse>> leave(@PathVariable String id) {
    String userId = currentUserProvider.getCurrentUser().userId();
    var updated = service.leave(userId, id);
    return ResponseEntity.ok(ApiResponse.success(200, "Left", mapper.toResponse(updated, userId)));
  }
}
