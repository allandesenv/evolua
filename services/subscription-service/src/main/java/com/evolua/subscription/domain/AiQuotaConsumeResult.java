package com.evolua.subscription.domain;

public record AiQuotaConsumeResult(Boolean allowed, String consumptionType, AiQuotaStatus status) {}
