package com.evolua.user.domain;

import java.time.Instant;
import java.time.LocalDate;

public record Profile(
    Long id,
    String userId,
    String displayName,
    String bio,
    Integer journeyLevel,
    Boolean premium,
    LocalDate birthDate,
    String gender,
    String customGender,
    String avatarUrl,
    Instant createdAt) {}
