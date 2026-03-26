package com.evolua.notification.domain; import java.time.Instant; public record NotificationJob(String id, String userId, String channel, String message, Instant createdAt) { }
