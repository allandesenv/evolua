package com.evolua.ai.application;

public record CheckInInsight(
    String insight,
    String suggestedAction,
    String riskLevel,
    Long suggestedTrailId,
    String suggestedTrailTitle,
    String suggestedTrailReason,
    Boolean fallbackUsed) {}
