package com.evolua.ai.application;

import java.time.Instant;

public record RecentCheckInSnapshot(
    Long id,
    String mood,
    String reflection,
    Integer energyLevel,
    String recommendedPractice,
    Instant createdAt) {}
