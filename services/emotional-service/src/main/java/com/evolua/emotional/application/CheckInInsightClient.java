package com.evolua.emotional.application;

import com.evolua.emotional.domain.CheckIn;
import java.util.List;
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
          suggestedSpace(response.get("suggestedSpace")),
          journeyPlan(response.get("journeyPlan")),
          generatedTrailDraft(response.get("generatedTrailDraft")),
          booleanValue(response.get("fallbackUsed")),
          booleanValue(response.get("quotaLimited")),
          intValue(response.get("quotaRemainingToday")),
          booleanValue(response.get("rewardedAdAvailable")),
          booleanValue(response.get("upgradeRecommended")),
          nullableStringValue(response.get("limitMessage")));
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
        null,
        null,
        null,
        true);
  }

  @SuppressWarnings("unchecked")
  private CheckInAiSuggestedSpace suggestedSpace(Object value) {
    if (!(value instanceof Map<?, ?> raw)) {
      return null;
    }
    var map = (Map<String, Object>) raw;
    return new CheckInAiSuggestedSpace(
        stringValue(map.get("id")),
        stringValue(map.get("slug")),
        stringValue(map.get("name")),
        stringValue(map.get("reason")));
  }

  @SuppressWarnings("unchecked")
  private CheckInAiJourneyPlan journeyPlan(Object value) {
    if (!(value instanceof Map<?, ?> raw)) {
      return null;
    }
    var map = (Map<String, Object>) raw;
    return new CheckInAiJourneyPlan(
        stringValue(map.get("journeyKey")),
        stringValue(map.get("journeyTitle")),
        stringValue(map.get("phaseLabel")),
        stringValue(map.get("continuityMode")),
        stringValue(map.get("summary")),
        stringValue(map.get("nextCheckInPrompt")));
  }

  @SuppressWarnings("unchecked")
  private CheckInAiGeneratedTrailDraft generatedTrailDraft(Object value) {
    if (!(value instanceof Map<?, ?> raw)) {
      return null;
    }
    var map = (Map<String, Object>) raw;
    var mediaLinks =
        map.get("mediaLinks") instanceof List<?> rawLinks
            ? rawLinks.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(
                    item ->
                        new CheckInAiGeneratedTrailDraftLink(
                            stringValue(item.get("label")),
                            stringValue(item.get("url")),
                            stringValue(item.get("type"))))
                .toList()
            : List.<CheckInAiGeneratedTrailDraftLink>of();
    return new CheckInAiGeneratedTrailDraft(
        stringValue(map.get("title")),
        stringValue(map.get("summary")),
        stringValue(map.get("content")),
        stringValue(map.get("category")),
        stringValue(map.get("sourceStyle")),
        mediaLinks);
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

  private Integer intValue(Object value) {
    return value instanceof Number number ? number.intValue() : null;
  }

  private String nullableStringValue(Object value) {
    return value == null ? null : value.toString();
  }
}
