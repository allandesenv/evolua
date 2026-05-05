package com.evolua.user.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupportTicketRequest(
    String category,
    @NotBlank @Size(max = 160) String subject,
    @NotBlank @Size(max = 4000) String message) {}
