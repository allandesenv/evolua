package com.evolua.user.domain;

import java.time.Instant;

public record SupportTicket(
    Long id,
    String userId,
    String email,
    String category,
    String subject,
    String message,
    String status,
    Instant createdAt,
    Instant updatedAt) {}
