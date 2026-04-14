package com.evolua.subscription.domain;

import java.time.Instant;

public record BillingCheckout(
    Long id,
    String publicId,
    String userId,
    String planCode,
    String billingCycle,
    String provider,
    String status,
    Boolean premium,
    String providerPreferenceId,
    String providerPaymentId,
    String checkoutUrl,
    String failureReason,
    Instant createdAt,
    Instant updatedAt,
    Instant confirmedAt) {}
