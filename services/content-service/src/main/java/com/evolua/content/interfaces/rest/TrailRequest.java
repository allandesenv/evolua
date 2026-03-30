package com.evolua.content.interfaces.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TrailRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(min = 12, max = 400) String summary,
    @NotBlank String content,
    @NotBlank @Size(max = 255) String category,
    @NotNull Boolean premium,
    @Size(max = 6) List<@Valid TrailMediaLinkRequest> mediaLinks) {}
