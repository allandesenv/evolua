package com.evolua.auth.domain;

import java.time.Instant;
import java.util.List;

public record AuthUser(
    Long id,
    String userId,
    String email,
    String passwordHash,
    String provider,
    String providerSubject,
    String displayName,
    String avatarUrl,
    List<String> roles,
    Instant createdAt) {}
