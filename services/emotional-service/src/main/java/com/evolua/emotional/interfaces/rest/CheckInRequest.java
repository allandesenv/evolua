package com.evolua.emotional.interfaces.rest;

public record CheckInRequest(
    @jakarta.validation.constraints.NotBlank String mood,
    String reflection,
    @jakarta.validation.constraints.NotNull Integer energyLevel) {}
