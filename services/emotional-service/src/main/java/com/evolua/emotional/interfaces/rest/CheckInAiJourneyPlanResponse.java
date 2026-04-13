package com.evolua.emotional.interfaces.rest;

public record CheckInAiJourneyPlanResponse(
    String journeyKey,
    String journeyTitle,
    String phaseLabel,
    String continuityMode,
    String summary,
    String nextCheckInPrompt) {}
