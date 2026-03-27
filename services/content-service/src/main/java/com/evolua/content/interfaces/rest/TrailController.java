package com.evolua.content.interfaces.rest;

import com.evolua.content.application.TrailService;
import com.evolua.content.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/trails")
public class TrailController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "title", "category", "premium", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final TrailService service;
  private final TrailMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public TrailController(TrailService service, TrailMapper mapper, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create Trail")
  public ResponseEntity<ApiResponse<TrailResponse>> create(@Valid @RequestBody TrailRequest request) {
    var created =
        service.create(
            currentUserProvider.getCurrentUser().userId(),
            request.title(),
            request.description(),
            request.category(),
            request.premium());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", mapper.toResponse(created)));
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

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
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
                mapper::toResponse,
                query.effectiveSortBy(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
                query.normalizedSortDir(),
                filters)));
  }
}
