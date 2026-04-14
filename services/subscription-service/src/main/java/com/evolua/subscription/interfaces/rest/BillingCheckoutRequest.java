package com.evolua.subscription.interfaces.rest;

public record BillingCheckoutRequest(
    @jakarta.validation.constraints.NotBlank String planCode, String frontendBaseUrl) {}
