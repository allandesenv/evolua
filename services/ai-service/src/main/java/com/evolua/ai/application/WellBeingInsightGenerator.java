package com.evolua.ai.application;

import java.util.List;
import com.evolua.ai.infrastructure.security.AuthenticatedUser;

public interface WellBeingInsightGenerator {
  CheckInInsight generate(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      List<TrailCandidate> candidates,
      List<SpaceCandidate> spaces,
      AuthenticatedUser currentUser);
}
