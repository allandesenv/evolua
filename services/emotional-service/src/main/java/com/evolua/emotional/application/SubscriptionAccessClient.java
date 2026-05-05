package com.evolua.emotional.application;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SubscriptionAccessClient {
  private final RestClient restClient;
  private final String internalToken;

  public SubscriptionAccessClient(
      @Value("${app.subscription.base-url:http://subscription-service:8087}") String subscriptionBaseUrl,
      @Value("${app.subscription.internal-token:change-me-internal-token}") String internalToken) {
    this.restClient = RestClient.builder().baseUrl(subscriptionBaseUrl).build();
    this.internalToken = internalToken;
  }

  public boolean hasPremiumAccess(String userId) {
    try {
      var response =
          restClient
              .get()
              .uri(uriBuilder -> uriBuilder.path("/v1/internal/subscription/access").queryParam("userId", userId).build())
              .header("X-Internal-Token", internalToken)
              .retrieve()
              .body(Map.class);
      return response != null
          && response.get("data") instanceof Map<?, ?> data
          && Boolean.TRUE.equals(data.get("premium"));
    } catch (Exception exception) {
      return false;
    }
  }
}
