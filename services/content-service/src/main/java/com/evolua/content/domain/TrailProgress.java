package com.evolua.content.domain;

import java.time.Instant;
import java.util.List;

public record TrailProgress(
    Long id,
    String userId,
    Long trailId,
    Integer currentStepIndex,
    List<Integer> completedStepIndexes,
    Instant startedAt,
    Instant updatedAt,
    Instant completedAt) {}
