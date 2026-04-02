package com.evolua.emotional.application;

import com.evolua.emotional.domain.CheckIn;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class CheckInInsightClient {
  private final RestClient restClient;
  private final String aiBaseUrl;

  public CheckInInsightClient(@Value("${app.ai.base-url:http://ai-service:8089}") String aiBaseUrl) {
    this.restClient = RestClient.builder().build();
    this.aiBaseUrl = aiBaseUrl;
  }

  @SuppressWarnings("unchecked")
  public CheckInAiInsight generateInsight(String authorizationHeader, CheckIn checkIn) {
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      return fallback(checkIn);
    }

    try {
      Map<String, Object> response =
          restClient
              .post()
              .uri(
                  UriComponentsBuilder.fromUriString(aiBaseUrl)
                      .path("/internal/check-in-insights")
                      .build()
                      .toUri())
              .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
              .body(
                  Map.of(
                      "mood", checkIn.mood(),
                      "reflection", checkIn.reflection(),
                      "energyLevel", checkIn.energyLevel()))
              .retrieve()
              .body(Map.class);

      if (response == null) {
        return fallback(checkIn);
      }

      return new CheckInAiInsight(
          stringValue(response.get("insight")),
          stringValue(response.get("suggestedAction")),
          stringValue(response.get("riskLevel")),
          longValue(response.get("suggestedTrailId")),
          stringValue(response.get("suggestedTrailTitle")),
          stringValue(response.get("suggestedTrailReason")),
          booleanValue(response.get("fallbackUsed")));
    } catch (Exception exception) {
      return fallback(checkIn);
    }
  }

  private CheckInAiInsight fallback(CheckIn checkIn) {
    return new CheckInAiInsight(
        "Seu check-in foi salvo e vale manter uma proxima acao pequena agora.",
        checkIn.energyLevel() <= 4
            ? "Comece por uma pausa curta de regulacao antes de ampliar o ritmo."
            : "Escolha uma unica acao leve para sustentar o restante do dia.",
        "low",
        null,
        null,
        "A recomendacao detalhada ficou indisponivel neste momento, entao mantivemos uma orientacao segura e curta.",
        true);
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private Long longValue(Object value) {
    return value instanceof Number number ? number.longValue() : null;
  }

  private Boolean booleanValue(Object value) {
    return value instanceof Boolean bool ? bool : Boolean.FALSE;
  }
}
