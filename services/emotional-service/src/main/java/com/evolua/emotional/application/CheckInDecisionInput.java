package com.evolua.emotional.application;

public record CheckInDecisionInput(
    String userId,
    String emotion,
    Integer intensity,
    String energy,
    String context,
    String note,
    Integer energyLevel) {}
