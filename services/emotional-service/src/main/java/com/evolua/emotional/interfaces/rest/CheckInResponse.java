package com.evolua.emotional.interfaces.rest;

import java.time.Instant;

public record CheckInResponse(
    Long id,
    String userId,
    String mood,
    String reflection,
    Integer energyLevel,
    String recommendedPractice,
    String emotion,
    Integer intensity,
    String energy,
    String context,
    String decisionTags,
    String severityLevel,
    CheckInAiInsightResponse aiInsight,
    Instant createdAt) {}
