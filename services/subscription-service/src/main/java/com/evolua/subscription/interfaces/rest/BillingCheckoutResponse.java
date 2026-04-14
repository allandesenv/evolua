package com.evolua.subscription.interfaces.rest;

import java.time.Instant;

public record BillingCheckoutResponse(
    String id,
    String planCode,
    String billingCycle,
    String status,
    Boolean premium,
    String checkoutUrl,
    String failureReason,
    Instant createdAt,
    Instant updatedAt,
    Instant confirmedAt) {}
