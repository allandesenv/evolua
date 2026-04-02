package com.evolua.auth.domain;

import java.time.Instant;

public record OAuthLoginState(
    Long id,
    String state,
    String frontendRedirectUri,
    Instant createdAt,
    Instant expiresAt) {}
