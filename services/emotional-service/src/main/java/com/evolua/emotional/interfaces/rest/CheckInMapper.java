package com.evolua.emotional.interfaces.rest;

import com.evolua.emotional.application.CheckInAiInsight;
import com.evolua.emotional.domain.CheckIn;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CheckInMapper {
  default CheckInResponse toResponse(CheckIn item) {
    return toResponse(item, null);
  }

  default CheckInResponse toResponse(CheckIn item, CheckInAiInsight aiInsight) {
    return new CheckInResponse(
        item.id(),
        item.userId(),
        item.mood(),
        item.reflection(),
        item.energyLevel(),
        item.recommendedPractice(),
        aiInsight == null
            ? null
            : new CheckInAiInsightResponse(
                aiInsight.insight(),
                aiInsight.suggestedAction(),
                aiInsight.riskLevel(),
                aiInsight.suggestedTrailId(),
                aiInsight.suggestedTrailTitle(),
                aiInsight.suggestedTrailReason(),
                aiInsight.fallbackUsed()),
        item.createdAt());
  }
}
