package com.evolua.ai.application;

import java.util.List;

public record EmotionalContextSnapshot(
    List<RecentCheckInSnapshot> recentCheckIns,
    Integer averageEnergy,
    String dominantMood,
    String energyTrendLabel) {}
