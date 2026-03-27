package com.evolua.user.interfaces.rest;

import com.evolua.user.application.ProfileService;
import com.evolua.user.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/profiles")
public class ProfileController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "displayName", "journeyLevel", "premium", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final ProfileService service;
  private final ProfileMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public ProfileController(ProfileService service, ProfileMapper mapper, CurrentUserProvider currentUserProvider) {
    this.service = service;
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
}
