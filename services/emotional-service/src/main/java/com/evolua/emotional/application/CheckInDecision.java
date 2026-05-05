package com.evolua.emotional.application;

import java.util.List;

public record CheckInDecision(
    String emotionalStateLabel,
    String shortInsight,
    String nextStep,
    CheckInSuggestedTrail suggestedTrail,
    CheckInSuggestedAction suggestedAction,
    String severityLevel,
    List<String> tags,
    boolean shouldSuggestAIChat,
    boolean shouldSuggestHistoryAnalysis) {}
