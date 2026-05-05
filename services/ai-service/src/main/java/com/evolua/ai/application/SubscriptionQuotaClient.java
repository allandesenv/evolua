package com.evolua.ai.application;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SubscriptionQuotaClient {
  private final RestClient restClient;
  private final String internalToken;

  public SubscriptionQuotaClient(
      @Value("${app.subscription.base-url:http://subscription-service:8087}") String subscriptionBaseUrl,
      @Value("${app.subscription.internal-token:change-me-internal-token}") String internalToken) {
    this.restClient = RestClient.builder().baseUrl(subscriptionBaseUrl).build();
    this.internalToken = internalToken;
  }

  public AiQuotaDecision consume(String userId) {
    try {
      var response =
          restClient
              .post()
              .uri("/v1/internal/ai-quota/consume")
              .header("X-Internal-Token", internalToken)
              .body(Map.of("userId", userId, "resource", "AI_ACTION"))
              .retrieve()
              .body(Map.class);
      if (response == null || !(response.get("data") instanceof Map<?, ?> rawData)) {
        return AiQuotaDecision.unavailable();
      }
      var data = (Map<?, ?>) rawData;
      var status = data.get("status") instanceof Map<?, ?> rawStatus ? rawStatus : Map.of();
      return new AiQuotaDecision(
          Boolean.TRUE.equals(data.get("allowed")),
          Boolean.TRUE.equals(status.get("premium")),
          numberValue(status.get("remainingToday")),
          Boolean.TRUE.equals(status.get("rewardedAdAvailable")),
          Boolean.TRUE.equals(status.get("upgradeRecommended")),
          stringValue(status.get("limitMessage")));
    } catch (Exception exception) {
      return AiQuotaDecision.unavailable();
    }
  }

  private Integer numberValue(Object value) {
    return value instanceof Number number ? number.intValue() : 0;
  }

  private String stringValue(Object value) {
    return value == null ? null : value.toString();
  }
}
