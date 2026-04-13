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
                aiInsight.suggestedSpace() == null
                    ? null
                    : new CheckInAiSuggestedSpaceResponse(
                        aiInsight.suggestedSpace().id(),
                        aiInsight.suggestedSpace().slug(),
                        aiInsight.suggestedSpace().name(),
                        aiInsight.suggestedSpace().reason()),
                aiInsight.journeyPlan() == null
                    ? null
                    : new CheckInAiJourneyPlanResponse(
                        aiInsight.journeyPlan().journeyKey(),
                        aiInsight.journeyPlan().journeyTitle(),
                        aiInsight.journeyPlan().phaseLabel(),
                        aiInsight.journeyPlan().continuityMode(),
                        aiInsight.journeyPlan().summary(),
                        aiInsight.journeyPlan().nextCheckInPrompt()),
                aiInsight.generatedTrailDraft() == null
                    ? null
                    : new CheckInAiGeneratedTrailDraftResponse(
                        aiInsight.generatedTrailDraft().title(),
                        aiInsight.generatedTrailDraft().summary(),
                        aiInsight.generatedTrailDraft().content(),
                        aiInsight.generatedTrailDraft().category(),
                        aiInsight.generatedTrailDraft().sourceStyle(),
                        aiInsight.generatedTrailDraft().mediaLinks().stream()
                            .map(
                                link ->
                                    new CheckInAiGeneratedTrailDraftLinkResponse(
                                        link.label(), link.url(), link.type()))
                            .toList()),
                aiInsight.fallbackUsed()),
        item.createdAt());
  }
}
