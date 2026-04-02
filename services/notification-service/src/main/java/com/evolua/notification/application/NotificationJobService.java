package com.evolua.notification.application;

import com.evolua.notification.domain.NotificationJob;
import com.evolua.notification.domain.NotificationJobRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationJobService {
  private final NotificationJobRepository repository;

  public NotificationJobService(NotificationJobRepository repository) {
    this.repository = repository;
  }

  public NotificationJob create(String userId, String channel, String message) {
    return repository.save(new NotificationJob(UUID.randomUUID().toString(), userId, channel, message, Instant.now()));
  }

  public Page<NotificationJob> list(String userId, Pageable pageable, String search, String channel) {
    return repository.findAllByUserId(userId, pageable, search, channel);
  }
}
