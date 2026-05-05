package com.evolua.subscription.domain;

import java.time.Instant;
import java.time.LocalDate;

public record AiUsageLedger(
    Long id,
    String userId,
    String resource,
    LocalDate usageDate,
    Integer baseUsed,
    Integer rewardUsed,
    Integer rewardGranted,
    Instant createdAt,
    Instant updatedAt) {}
