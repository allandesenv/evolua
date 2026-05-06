package com.evolua.content.application;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SubscriptionAccessClient {
  private final RestClient restClient;
  private final String internalToken;

  public SubscriptionAccessClient(
      @Value("${app.subscription.base-url:http://localhost:8087}") String subscriptionBaseUrl,
      @Value("${app.subscription.internal-token:change-me-internal-token}") String internalToken) {
    this.restClient = RestClient.builder().baseUrl(subscriptionBaseUrl).build();
    this.internalToken = internalToken;
  }

  public AccessSummary accessSummary(String userId) {
    var response =
        restClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/v1/internal/subscription/access").queryParam("userId", userId).build())
            .header("X-Internal-Token", internalToken)
            .retrieve()
            .body(Map.class);

    if (response == null || !(response.get("data") instanceof Map<?, ?> data)) {
      return new AccessSummary(false, false);
    }

    var premium = data.get("premium");
    var mentorPass = data.get("mentorPremiumPassActive");
    return new AccessSummary(Boolean.TRUE.equals(premium), Boolean.TRUE.equals(mentorPass));
  }

  public boolean hasPremiumAccess(String userId) {
    return accessSummary(userId).premium();
  }

  public record AccessSummary(boolean premium, boolean mentorPremiumPassActive) {}
}
