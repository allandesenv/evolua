package com.evolua.content.interfaces.rest;

import java.time.Instant;
import java.util.List;

public record TrailProgressResponse(
    Integer currentStepIndex,
    List<Integer> completedStepIndexes,
    Instant startedAt,
    Instant updatedAt,
    Instant completedAt) {}
