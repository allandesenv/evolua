package com.evolua.subscription.domain;

import java.time.Instant;

public record BillingEvent(
    Long id,
    String provider,
    String providerEventId,
    String eventType,
    String checkoutPublicId,
    String payloadJson,
    Instant createdAt) {}
