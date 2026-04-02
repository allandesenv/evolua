package com.evolua.ai.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EmotionalContextClient {
  private final RestClient restClient;
  private final String emotionalBaseUrl;

  public EmotionalContextClient(
      @Value("${app.ai.emotional-base-url:http://emotional-service:8084}") String emotionalBaseUrl) {
    this.restClient = RestClient.builder().build();
    this.emotionalBaseUrl = emotionalBaseUrl;
  }

  @SuppressWarnings("unchecked")
  public EmotionalContextSnapshot fetchRecentContext(String authorizationHeader) {
    var uri =
        UriComponentsBuilder.fromUriString(emotionalBaseUrl)
            .path("/v1/check-ins")
            .queryParam("page", 0)
            .queryParam("size", 6)
            .queryParam("sortBy", "createdAt")
            .queryParam("sortDir", "desc")
            .build()
            .toUri();

    Map<String, Object> payload =
        restClient
            .get()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .retrieve()
            .body(Map.class);

    var data = payload == null ? Map.<String, Object>of() : castMap(payload.get("data"));
    var items = data.get("items") instanceof List<?> rawItems ? rawItems : List.of();
    var recent = new ArrayList<RecentCheckInSnapshot>();
    for (var item : items) {
      var map = castMap(item);
      recent.add(
          new RecentCheckInSnapshot(
              longValue(map.get("id")),
              stringValue(map.get("mood")),
              stringValue(map.get("reflection")),
              intValue(map.get("energyLevel")),
              stringValue(map.get("recommendedPractice")),
              instantValue(map.get("createdAt"))));
    }

    var averageEnergy =
        recent.isEmpty()
            ? null
            : (int)
                Math.round(
                    recent.stream()
                        .mapToInt(item -> item.energyLevel() == null ? 0 : item.energyLevel())
                        .average()
                        .orElse(0));

    var dominantMood =
        recent.stream()
            .map(item -> normalize(item.mood()))
            .filter(mood -> !mood.isBlank())
            .collect(
                java.util.stream.Collectors.groupingBy(
                    mood -> mood, java.util.stream.Collectors.counting()))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("indefinido");

    return new EmotionalContextSnapshot(recent, averageEnergy, dominantMood, energyTrend(recent));
  }

  private String energyTrend(List<RecentCheckInSnapshot> items) {
    if (items.size() < 3) {
      return "inicial";
    }
    var midpoint = items.size() / 2;
    var recentAverage =
        items.subList(0, midpoint).stream()
            .mapToInt(item -> item.energyLevel() == null ? 0 : item.energyLevel())
            .average()
            .orElse(0);
    var olderAverage =
        items.subList(midpoint, items.size()).stream()
            .mapToInt(item -> item.energyLevel() == null ? 0 : item.energyLevel())
            .average()
            .orElse(recentAverage);
    if (recentAverage > olderAverage + 0.6) {
      return "subindo";
    }
    if (recentAverage < olderAverage - 0.6) {
      return "mais fragil";
    }
    return "estavel";
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> castMap(Object value) {
    return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString();
  }

  private Integer intValue(Object value) {
    return value instanceof Number number ? number.intValue() : null;
  }

  private Long longValue(Object value) {
    return value instanceof Number number ? number.longValue() : null;
  }

  private Instant instantValue(Object value) {
    return value == null ? null : Instant.parse(value.toString());
  }

  private String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }
}
