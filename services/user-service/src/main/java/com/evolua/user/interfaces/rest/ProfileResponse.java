package com.evolua.user.interfaces.rest;

import java.time.Instant;
import java.time.LocalDate;

public record ProfileResponse(
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
