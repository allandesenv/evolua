package com.evolua.user.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProfileMeRequest(
    @NotBlank String displayName,
    String bio,
    @NotNull Integer journeyLevel,
    @NotNull LocalDate birthDate,
    @NotBlank String gender,
    String customGender) {}
