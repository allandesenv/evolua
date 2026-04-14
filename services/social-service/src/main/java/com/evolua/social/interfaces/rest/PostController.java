package com.evolua.social.interfaces.rest;

import com.evolua.social.application.PostService;
import com.evolua.social.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/posts")
public class PostController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "community", "visibility", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final PostService service;
  private final PostMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public PostController(PostService service, PostMapper mapper, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create Post")
  public ResponseEntity<ApiResponse<PostResponse>> create(@Valid @RequestBody PostRequest request) {
    var created =
        service.create(
            currentUserProvider.getCurrentUser().userId(),
            request.content(),
            request.community(),
            request.visibility());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", mapper.toResponse(created)));
  }

  @GetMapping
  @Operation(summary = "List posts")
  public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String community,
      @RequestParam(required = false) String visibility,
      @RequestParam(required = false) Boolean mine) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (community != null && !community.isBlank()) {
      filters.put("community", community.trim());
    }
    if (visibility != null && !visibility.isBlank()) {
      filters.put("visibility", visibility.trim().toUpperCase());
    }
    if (mine != null) {
      filters.put("mine", mine);
    }

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            community == null ? null : community.trim(),
            visibility == null ? null : visibility.trim().toUpperCase(),
            mine);

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
