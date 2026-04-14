package com.evolua.ai.application;

public record JourneyPlan(
    String journeyKey,
    String journeyTitle,
    String phaseLabel,
    String continuityMode,
    String summary,
    String nextCheckInPrompt) {}
