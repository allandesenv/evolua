package com.evolua.auth.domain;

import java.time.Instant;

public record AuthAuthorizationCode(
    Long id,
    String code,
    String userId,
    Instant createdAt,
    Instant expiresAt,
    boolean consumed) {}
