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
public class SocialSpaceCatalogClient {
  private final RestClient restClient;
  private final String socialBaseUrl;

  public SocialSpaceCatalogClient(
      @Value("${app.ai.social-base-url:http://social-service:8085}") String socialBaseUrl) {
    this.restClient = RestClient.builder().build();
    this.socialBaseUrl = socialBaseUrl;
  }

  @SuppressWarnings("unchecked")
  public List<SpaceCandidate> fetchSpaceCandidates(String authorizationHeader) {
    var uri =
        UriComponentsBuilder.fromUriString(socialBaseUrl)
            .path("/v1/communities")
            .queryParam("page", 0)
            .queryParam("size", 16)
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
    var candidates = new ArrayList<SpaceCandidate>();
    for (var item : items) {
      var map = castMap(item);
      candidates.add(
          new SpaceCandidate(
              stringValue(map.get("id")),
              stringValue(map.get("slug")),
              stringValue(map.get("name")),
              stringValue(map.get("description")),
              stringValue(map.get("category")),
              stringValue(map.get("visibility")),
              booleanValue(map.get("joined"))));
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

  private Boolean booleanValue(Object value) {
    return value instanceof Boolean bool ? bool : Boolean.FALSE;
  }
}
