package com.evolua.ai.application;

public record AiQuotaDecision(
    Boolean allowed,
    Boolean premium,
    Integer remainingToday,
    Boolean rewardedAdAvailable,
    Boolean upgradeRecommended,
    String limitMessage) {
  public static AiQuotaDecision unavailable() {
    return new AiQuotaDecision(
        false,
        false,
        0,
        false,
        true,
        "Nao foi possivel validar sua quota de IA agora. Mantivemos uma orientacao segura sem custo.");
  }
}
