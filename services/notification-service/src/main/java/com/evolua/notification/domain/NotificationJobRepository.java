package com.evolua.notification.domain;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationJobRepository {
  NotificationJob save(NotificationJob item);

  Page<NotificationJob> findAllByUserId(String userId, Pageable pageable, String search, String type, Boolean unreadOnly);

  long countUnreadByUserId(String userId);

  NotificationJob markAsRead(String userId, String notificationId, Instant readAt);

  long markAllAsRead(String userId, Instant readAt);

  boolean existsRecentByUserIdAndType(String userId, String type, Instant since);
}
