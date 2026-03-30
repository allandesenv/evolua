package com.evolua.social.interfaces.rest;

import jakarta.validation.constraints.NotBlank;

public record CommunityRequest(
    @NotBlank String name,
    String slug,
    @NotBlank String description,
    @NotBlank String visibility,
    @NotBlank String category) {}
