package com.evolua.emotional.application;

import java.util.List;

public record EmotionalStateDefinition(
    String emotion,
    String valence,
    String activation,
    String need,
    String label,
    String insight,
    String nextStep,
    String trailId,
    String trailTitle,
    String actionType,
    String actionTitle,
    Integer actionDurationMinutes,
    List<String> tags) {
  boolean negative() {
    return "negativa".equals(valence);
  }

  boolean positive() {
    return "positiva".equals(valence);
  }
}
