package com.evolua.ai.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ContentCatalogClient {
  private final RestClient restClient;
  private final String contentBaseUrl;

  public ContentCatalogClient(
      @Value("${app.ai.content-base-url:http://content-service:8083}") String contentBaseUrl) {
    this.restClient = RestClient.builder().build();
    this.contentBaseUrl = contentBaseUrl;
  }

  @SuppressWarnings("unchecked")
  public List<TrailCandidate> fetchTrailCandidates(String authorizationHeader) {
    var uri =
        UriComponentsBuilder.fromUriString(contentBaseUrl)
            .path("/v1/trails")
            .queryParam("page", 0)
            .queryParam("size", 12)
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
    var candidates = new ArrayList<TrailCandidate>();
    for (var item : items) {
      var map = castMap(item);
      candidates.add(
          new TrailCandidate(
              longValue(map.get("id")),
              stringValue(map.get("title")),
              stringValue(map.get("summary")),
              stringValue(map.get("category")),
              booleanValue(map.get("premium")),
              booleanValue(map.get("accessible"))));
    }
    return candidates;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> castMap(Object value) {
    return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
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
