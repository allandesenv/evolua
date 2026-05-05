package com.evolua.user.interfaces.rest;

import jakarta.validation.constraints.NotBlank;

public record DeleteUserDataRequest(@NotBlank String userId) {}
