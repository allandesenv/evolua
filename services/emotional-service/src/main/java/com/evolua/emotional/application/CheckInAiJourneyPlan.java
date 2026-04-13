package com.evolua.emotional.application;

public record CheckInAiJourneyPlan(
    String journeyKey,
    String journeyTitle,
    String phaseLabel,
    String continuityMode,
    String summary,
    String nextCheckInPrompt) {}
