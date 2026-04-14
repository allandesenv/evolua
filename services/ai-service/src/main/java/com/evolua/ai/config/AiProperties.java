package com.evolua.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {
  private String provider = "heuristic";
  private String emotionalBaseUrl = "http://emotional-service:8084";
  private String contentBaseUrl = "http://content-service:8083";
  private String socialBaseUrl = "http://social-service:8085";
  private String baseUrl = "https://api.openai.com/v1";
  private String apiKey = "";
  private String model = "";
  private String language = "pt-BR";
  private Integer maxTokens = 800;
  private Double temperature = 0.3;
  private Integer timeoutSeconds = 15;
  private Boolean fallbackEnabled = Boolean.TRUE;

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getEmotionalBaseUrl() {
    return emotionalBaseUrl;
  }

  public void setEmotionalBaseUrl(String emotionalBaseUrl) {
    this.emotionalBaseUrl = emotionalBaseUrl;
  }

  public String getContentBaseUrl() {
    return contentBaseUrl;
  }

  public void setContentBaseUrl(String contentBaseUrl) {
    this.contentBaseUrl = contentBaseUrl;
  }

  public String getSocialBaseUrl() {
    return socialBaseUrl;
  }

  public void setSocialBaseUrl(String socialBaseUrl) {
    this.socialBaseUrl = socialBaseUrl;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Integer getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public void setTimeoutSeconds(Integer timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
  }

  public Boolean getFallbackEnabled() {
    return fallbackEnabled;
  }

  public void setFallbackEnabled(Boolean fallbackEnabled) {
    this.fallbackEnabled = fallbackEnabled;
  }
}
