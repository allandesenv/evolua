package com.evolua.subscription.interfaces.rest;

import com.evolua.subscription.application.SubscriptionService;
import com.evolua.subscription.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/subscriptions")
public class SubscriptionController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "planCode", "status", "billingCycle", "premium", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final SubscriptionService service;
  private final SubscriptionMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public SubscriptionController(
      SubscriptionService service, SubscriptionMapper mapper, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create Subscription")
  public ResponseEntity<ApiResponse<SubscriptionResponse>> create(
      @Valid @RequestBody SubscriptionRequest request) {
    var created =
        service.create(
            currentUserProvider.getCurrentUser().userId(),
            request.planCode(),
            request.status(),
            request.billingCycle(),
            request.premium());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", mapper.toResponse(created)));
  }

  @GetMapping
  @Operation(summary = "List subscriptions")
  public ResponseEntity<ApiResponse<PageResponse<SubscriptionResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Boolean premium) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (status != null && !status.isBlank()) {
      filters.put("status", status.trim());
    }
    if (premium != null) {
      filters.put("premium", premium);
    }

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            status == null ? null : status.trim(),
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
