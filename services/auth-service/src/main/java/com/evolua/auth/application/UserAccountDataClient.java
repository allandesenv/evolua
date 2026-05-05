package com.evolua.auth.application;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserAccountDataClient {
  private final RestClient restClient;
  private final String internalToken;

  public UserAccountDataClient(
      @Value("${app.user-service.base-url:http://user-service:8082}") String userServiceBaseUrl,
      @Value("${app.account.internal-token:change-me-internal-token}") String internalToken) {
    this.restClient = RestClient.builder().baseUrl(userServiceBaseUrl).build();
    this.internalToken = internalToken;
  }

  public void deleteUserData(String userId) {
    try {
      restClient
          .post()
          .uri("/v1/internal/users/delete-data")
          .header("X-Internal-Token", internalToken)
          .body(Map.of("userId", userId))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception ignored) {
      // Account deletion must still revoke auth access even if auxiliary data cleanup is retried later.
    }
  }
}
