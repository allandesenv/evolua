package com.evolua.emotional.application;

import com.evolua.emotional.domain.CheckIn;

public record CheckInCreationResult(CheckIn checkIn, CheckInAiInsight aiInsight) {}
