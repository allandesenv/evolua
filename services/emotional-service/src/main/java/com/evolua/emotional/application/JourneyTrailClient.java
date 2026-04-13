package com.evolua.emotional.application;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class JourneyTrailClient {
  private final RestClient restClient;
  private final String contentBaseUrl;

  public JourneyTrailClient(
      @Value("${app.content.base-url:http://content-service:8083}") String contentBaseUrl) {
    this.restClient = RestClient.builder().build();
    this.contentBaseUrl = contentBaseUrl;
  }

  @SuppressWarnings("unchecked")
  public JourneyTrailSummary upsertCurrentJourney(
      String authorizationHeader,
      CheckInAiGeneratedTrailDraft generatedTrailDraft,
      CheckInAiJourneyPlan journeyPlan) {
    if (authorizationHeader == null
        || authorizationHeader.isBlank()
        || generatedTrailDraft == null
        || journeyPlan == null) {
      return null;
    }

    try {
      Map<String, Object> response =
          restClient
              .post()
              .uri(
                  UriComponentsBuilder.fromUriString(contentBaseUrl)
                      .path("/v1/trails/internal/journey/current")
                      .build()
                      .toUri())
              .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
              .body(
                  Map.of(
                      "title", generatedTrailDraft.title(),
                      "summary", generatedTrailDraft.summary(),
                      "content", generatedTrailDraft.content(),
                      "category", generatedTrailDraft.category(),
                      "journeyKey", journeyPlan.journeyKey(),
                      "sourceStyle", generatedTrailDraft.sourceStyle(),
                      "mediaLinks",
                      generatedTrailDraft.mediaLinks() == null
                          ? List.of()
                          : generatedTrailDraft.mediaLinks().stream()
                              .map(link -> Map.of("label", link.label(), "url", link.url(), "type", link.type()))
                              .toList()))
              .retrieve()
              .body(Map.class);

      if (response == null || !(response.get("data") instanceof Map<?, ?> rawData)) {
        return null;
      }

      var data = (Map<String, Object>) rawData;
      return new JourneyTrailSummary(
          longValue(data.get("id")),
          stringValue(data.get("title")),
          stringValue(data.get("summary")),
          stringValue(data.get("category")),
          stringValue(data.get("journeyKey")),
          stringValue(data.get("sourceStyle")));
    } catch (Exception exception) {
      return null;
    }
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private Long longValue(Object value) {
    return value instanceof Number number ? number.longValue() : null;
  }

  public record JourneyTrailSummary(
      Long id,
      String title,
      String summary,
      String category,
      String journeyKey,
      String sourceStyle) {}
}
