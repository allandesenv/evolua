package com.evolua.user.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProfileRequest(
    @NotBlank String displayName,
    String bio,
    @NotNull Integer journeyLevel,
    @NotNull Boolean premium,
    LocalDate birthDate,
    String gender,
    String customGender) {}
