package com.evolua.ai.application;

import com.evolua.ai.infrastructure.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

@Service
public class CheckInInsightService {
  private final EmotionalContextClient emotionalContextClient;
  private final ContentCatalogClient contentCatalogClient;
  private final SocialSpaceCatalogClient socialSpaceCatalogClient;
  private final WellBeingInsightGenerator insightGenerator;

  public CheckInInsightService(
      EmotionalContextClient emotionalContextClient,
      ContentCatalogClient contentCatalogClient,
      SocialSpaceCatalogClient socialSpaceCatalogClient,
      WellBeingInsightGenerator insightGenerator) {
    this.emotionalContextClient = emotionalContextClient;
    this.contentCatalogClient = contentCatalogClient;
    this.socialSpaceCatalogClient = socialSpaceCatalogClient;
    this.insightGenerator = insightGenerator;
  }

  public CheckInInsight generateInsight(
      String authorizationHeader, AuthenticatedUser currentUser, CurrentCheckInInput currentCheckIn) {
    try {
      var context = emotionalContextClient.fetchRecentContext(authorizationHeader);
      var candidates = contentCatalogClient.fetchTrailCandidates(authorizationHeader);
      var spaces = socialSpaceCatalogClient.fetchSpaceCandidates(authorizationHeader);
      return insightGenerator.generate(currentCheckIn, context, candidates, spaces, currentUser);
    } catch (Exception exception) {
      return new CheckInInsight(
          "Registramos seu check-in e mantivemos uma orientacao segura para agora.",
          "Siga com uma pausa curta de regulacao e retome o app quando quiser.",
          "low",
          null,
          null,
          "A recomendacao detalhada ficou indisponivel neste momento, entao priorizamos um proximo passo simples.",
          null,
          null,
          null,
          true);
    }
  }
}
