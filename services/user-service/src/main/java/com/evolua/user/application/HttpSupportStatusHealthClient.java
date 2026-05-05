package com.evolua.user.application;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class HttpSupportStatusHealthClient implements SupportStatusHealthClient {
  private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

  @Override
  public boolean isHealthy(String url) {
    try {
      var request =
          HttpRequest.newBuilder(URI.create(url))
              .timeout(Duration.ofSeconds(2))
              .GET()
              .build();
      var response = client.send(request, HttpResponse.BodyHandlers.discarding());
      return response.statusCode() >= 200 && response.statusCode() < 300;
    } catch (Exception exception) {
      return false;
    }
  }
}
