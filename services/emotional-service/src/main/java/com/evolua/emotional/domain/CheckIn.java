package com.evolua.emotional.domain;

import java.time.Instant;

public record CheckIn(
    Long id,
    String userId,
    String mood,
    String reflection,
    Integer energyLevel,
    String recommendedPractice,
    Instant createdAt,
    String emotion,
    Integer intensity,
    String energy,
    String context,
    String decisionTags,
    String severityLevel) {
  public CheckIn(
      Long id,
      String userId,
      String mood,
      String reflection,
      Integer energyLevel,
      String recommendedPractice,
      Instant createdAt) {
    this(
        id,
        userId,
        mood,
        reflection,
        energyLevel,
        recommendedPractice,
        createdAt,
        mood,
        null,
        null,
        null,
        null,
        null);
  }
}
