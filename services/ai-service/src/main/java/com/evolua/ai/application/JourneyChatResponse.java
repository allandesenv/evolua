package com.evolua.ai.application;

public record JourneyChatResponse(String reply, String riskLevel, String suggestedNextStep, Boolean fallbackUsed) {}
