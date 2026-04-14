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
import org.springframework.web.server.ResponseStatusException;

@RestController
public class NotificationJobController {
  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("title", "message", "type", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final NotificationJobService service;
  private final CurrentUserProvider currentUserProvider;

  public NotificationJobController(NotificationJobService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping("/v1/notifications")
  @Operation(summary = "List notifications for current user")
  public ResponseEntity<ApiResponse<PageResponse<NotificationJob>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Boolean unreadOnly) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (type != null && !type.isBlank()) {
      filters.put("type", type.trim().toUpperCase());
    }
    if (unreadOnly != null) {
      filters.put("unreadOnly", unreadOnly);
    }

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            type,
            unreadOnly);

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

  @GetMapping("/v1/notifications/unread-count")
  @Operation(summary = "Unread notifications count")
  public ResponseEntity<ApiResponse<UnreadCountResponse>> unreadCount() {
    var userId = currentUserProvider.getCurrentUser().userId();
    return ResponseEntity.ok(
        ApiResponse.success(200, "Counted", new UnreadCountResponse(service.countUnread(userId))));
  }

  @PostMapping("/v1/notifications/{notificationId}/read")
  @Operation(summary = "Mark notification as read")
  public ResponseEntity<ApiResponse<NotificationJob>> markAsRead(@PathVariable String notificationId) {
    var userId = currentUserProvider.getCurrentUser().userId();
    var updated = service.markAsRead(userId, notificationId);
    return ResponseEntity.ok(ApiResponse.success(200, "Updated", updated));
  }

  @PostMapping("/v1/notifications/read-all")
  @Operation(summary = "Mark all notifications as read")
  public ResponseEntity<ApiResponse<ReadAllResponse>> markAllAsRead() {
    var userId = currentUserProvider.getCurrentUser().userId();
    return ResponseEntity.ok(
        ApiResponse.success(200, "Updated", new ReadAllResponse(service.markAllAsRead(userId))));
  }

  @PostMapping("/v1/admin/notifications")
  @Operation(summary = "Create notification manually as admin")
  public ResponseEntity<ApiResponse<NotificationJob>> createAdmin(
      @Valid @RequestBody AdminNotificationRequest request) {
    var currentUser = currentUserProvider.getCurrentUser();
    if (!service.isAdmin(currentUser.roles())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can send notifications");
    }

    var created =
        service.createAdmin(
            currentUser.userId(),
            request.targetUserId(),
            request.type(),
            request.title(),
            request.message(),
            request.actionTarget());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Created", created));
  }

  public record AdminNotificationRequest(
      @NotBlank String targetUserId,
      @NotBlank String type,
      @NotBlank String title,
      @NotBlank String message,
      String actionTarget) {}

  public record UnreadCountResponse(long unreadCount) {}

  public record ReadAllResponse(long updatedCount) {}
}
