package com.evolua.emotional.interfaces.rest;

import com.evolua.emotional.application.CheckInService;
import com.evolua.emotional.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/check-ins")
public class CheckInController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "mood", "energyLevel", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final CheckInService service;
  private final CheckInMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public CheckInController(CheckInService service, CheckInMapper mapper, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create CheckIn")
  public ResponseEntity<ApiResponse<CheckInResponse>> create(@Valid @RequestBody CheckInRequest request) {
    var created =
        service.create(
            currentUserProvider.getCurrentUser().userId(),
            request.mood(),
            request.reflection(),
            request.energyLevel(),
            request.recommendedPractice());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", mapper.toResponse(created)));
  }

  @GetMapping
  @Operation(summary = "List check-ins")
  public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String mood) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (mood != null && !mood.isBlank()) {
      filters.put("mood", mood.trim());
    }

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            mood == null ? null : mood.trim());

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
