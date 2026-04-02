package com.evolua.emotional.interfaces.rest;

public record CheckInAiInsightResponse(
    String insight,
    String suggestedAction,
    String riskLevel,
    Long suggestedTrailId,
    String suggestedTrailTitle,
    String suggestedTrailReason,
    Boolean fallbackUsed) {}
