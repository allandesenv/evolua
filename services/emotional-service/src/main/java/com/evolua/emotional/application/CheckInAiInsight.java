package com.evolua.emotional.application;

public record CheckInAiInsight(
    String insight,
    String suggestedAction,
    String riskLevel,
    Long suggestedTrailId,
    String suggestedTrailTitle,
    String suggestedTrailReason,
    Boolean fallbackUsed) {}
