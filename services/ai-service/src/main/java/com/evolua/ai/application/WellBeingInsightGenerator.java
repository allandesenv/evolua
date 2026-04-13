package com.evolua.ai.application;

import java.util.List;

public interface WellBeingInsightGenerator {
  CheckInInsight generate(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      List<TrailCandidate> candidates,
      List<SpaceCandidate> spaces,
      List<String> roles);
}
