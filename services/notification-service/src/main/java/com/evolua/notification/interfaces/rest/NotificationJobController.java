package com.evolua.notification.interfaces.rest;

import com.evolua.notification.application.NotificationJobService;
import com.evolua.notification.domain.NotificationJob;
import com.evolua.notification.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications")
public class NotificationJobController {
  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("channel", "message", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final NotificationJobService service;
  private final CurrentUserProvider currentUserProvider;

  public NotificationJobController(NotificationJobService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create notification")
  public ResponseEntity<ApiResponse<NotificationJob>> create(@Valid @RequestBody NotificationJobRequest request) {
    NotificationJob created =
        service.create(currentUserProvider.getCurrentUser().userId(), request.channel(), request.message());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Created", created));
  }

  @GetMapping
  @Operation(summary = "List notifications")
  public ResponseEntity<ApiResponse<PageResponse<NotificationJob>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String channel) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (channel != null && !channel.isBlank()) {
      filters.put("channel", channel.trim().toUpperCase());
    }

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            channel == null ? null : channel.trim().toUpperCase());

    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Listed",
            new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious(),
                query.effectiveSortBy(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
                query.normalizedSortDir(),
                filters)));
  }

  public record NotificationJobRequest(@NotBlank String channel, @NotBlank String message) {}
}
