package com.evolua.emotional.interfaces.rest;

public record CheckInRequest(
    @jakarta.validation.constraints.NotBlank String mood,
    String reflection,
    @jakarta.validation.constraints.NotNull Integer energyLevel,
    String emotion,
    @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(10) Integer intensity,
    String energy,
    String context,
    String note) {}
