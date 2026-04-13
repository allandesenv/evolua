package com.evolua.emotional.interfaces.rest;

public record CheckInAiSuggestedSpaceResponse(
    String id,
    String slug,
    String name,
    String reason) {}
