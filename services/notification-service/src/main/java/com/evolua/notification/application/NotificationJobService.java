package com.evolua.notification.application;

import com.evolua.notification.domain.NotificationJob;
import com.evolua.notification.domain.NotificationJobRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationJobService {
  private static final List<String> ALLOWED_TYPES =
      List.of("CHECKIN_REMINDER", "EVENT", "ADMIN_MESSAGE");

  private final NotificationJobRepository repository;

  public NotificationJobService(NotificationJobRepository repository) {
    this.repository = repository;
  }

  public NotificationJob createSystem(
      String userId, String type, String title, String message, String actionTarget) {
    return create(userId, type, title, message, actionTarget, "SYSTEM", null);
  }

  public NotificationJob createAdmin(
      String createdBy, String userId, String type, String title, String message, String actionTarget) {
    return create(userId, type, title, message, actionTarget, "ADMIN", createdBy);
  }

  public Page<NotificationJob> list(
      String userId, Pageable pageable, String search, String type, Boolean unreadOnly) {
    return repository.findAllByUserId(userId, pageable, search, normalizeType(type), unreadOnly);
  }

  public long countUnread(String userId) {
    return repository.countUnreadByUserId(userId);
  }

  public NotificationJob markAsRead(String userId, String notificationId) {
    return repository.markAsRead(userId, notificationId, Instant.now());
  }

  public long markAllAsRead(String userId) {
    return repository.markAllAsRead(userId, Instant.now());
  }

  public boolean isAdmin(List<String> roles) {
    return roles != null && roles.contains("ROLE_ADMIN");
  }

  public boolean hasRecentReminder(String userId, String type, Instant since) {
    return repository.existsRecentByUserIdAndType(userId, normalizeType(type), since);
  }

  private NotificationJob create(
      String userId,
      String type,
      String title,
      String message,
      String actionTarget,
      String source,
      String createdBy) {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("targetUserId must not be blank");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (message == null || message.isBlank()) {
      throw new IllegalArgumentException("message must not be blank");
    }

    return repository.save(
        new NotificationJob(
            UUID.randomUUID().toString(),
            userId.trim(),
            normalizeType(type),
            title.trim(),
            message.trim(),
            normalizeActionTarget(actionTarget),
            source,
            createdBy,
            Instant.now(),
            null));
  }

  private String normalizeType(String type) {
    var normalized = type == null ? "" : type.trim().toUpperCase();
    if (!ALLOWED_TYPES.contains(normalized)) {
      throw new IllegalArgumentException("type must be one of " + ALLOWED_TYPES);
    }
    return normalized;
  }

  private String normalizeActionTarget(String actionTarget) {
    if (actionTarget == null || actionTarget.isBlank()) {
      return null;
    }
    return actionTarget.trim();
  }
}
