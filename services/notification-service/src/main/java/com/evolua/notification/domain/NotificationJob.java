package com.evolua.notification.domain;

import java.time.Instant;

public record NotificationJob(
    String id,
    String userId,
    String type,
    String title,
    String message,
    String actionTarget,
    String source,
    String createdBy,
    Instant createdAt,
    Instant readAt) {}
