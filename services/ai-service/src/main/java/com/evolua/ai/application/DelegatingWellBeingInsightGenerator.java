package com.evolua.ai.application;

import com.evolua.ai.config.AiProperties;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DelegatingWellBeingInsightGenerator implements WellBeingInsightGenerator {
  private final AiProperties aiProperties;
  private final RuleBasedWellBeingInsightGenerator heuristicGenerator;
  private final OpenAiWellBeingInsightGenerator openAiGenerator;

  public DelegatingWellBeingInsightGenerator(
      AiProperties aiProperties,
      RuleBasedWellBeingInsightGenerator heuristicGenerator,
      OpenAiWellBeingInsightGenerator openAiGenerator) {
    this.aiProperties = aiProperties;
    this.heuristicGenerator = heuristicGenerator;
    this.openAiGenerator = openAiGenerator;
  }

  @Override
  public CheckInInsight generate(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      List<TrailCandidate> candidates,
      List<SpaceCandidate> spaces,
      List<String> roles) {
    var provider = aiProperties.getProvider() == null ? "" : aiProperties.getProvider().trim();
    return switch (provider.toLowerCase(Locale.ROOT)) {
      case "openai" -> openAiGenerator.generate(currentCheckIn, context, candidates, spaces, roles);
      default -> heuristicGenerator.generate(currentCheckIn, context, candidates, spaces, roles);
    };
  }
}
