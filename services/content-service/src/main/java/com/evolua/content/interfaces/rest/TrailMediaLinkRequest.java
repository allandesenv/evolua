package com.evolua.content.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrailMediaLinkRequest(
    @NotBlank @Size(max = 120) String label,
    @NotBlank @Size(max = 2048) String url,
    @NotBlank @Size(max = 20) String type) {}
