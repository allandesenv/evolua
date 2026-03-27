package com.evolua.notification.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationJobRepository {
  NotificationJob save(NotificationJob item);

  Page<NotificationJob> findAllByUserId(String userId, Pageable pageable, String search, String channel);
}
