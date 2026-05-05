package com.evolua.subscription.application;

import com.evolua.subscription.domain.PlanCatalogItem;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlanCatalogService {
  private static final List<PlanCatalogItem> PLANS =
      List.of(
          new PlanCatalogItem(
              "essential-free",
              "Essencial",
              "Base gratuita com check-ins ilimitados, anuncios recompensados e limites inteligentes de IA.",
              "MONTHLY",
              false,
              BigDecimal.ZERO,
              "BRL",
              List.of(
                  "Check-ins simples ilimitados",
                  "1 analise de IA por dia",
                  "+1 analise extra por dia via anuncio recompensado",
                  "Historico dos ultimos 30 dias",
                  "Trilhas basicas com anuncios"),
              true),
          new PlanCatalogItem(
              "premium-monthly",
              "Premium Mensal",
              "Aprofunda a jornada sem anuncios, com mais IA, historico completo e conteudos premium.",
              "MONTHLY",
              true,
              new BigDecimal("29.90"),
              "BRL",
              List.of(
                  "Sem anuncios",
                  "10 acoes de IA por dia",
                  "Historico emocional completo",
                  "Trilhas personalizadas e premium",
                  "Relatorios emocionais e exportacao futuramente"),
              true),
          new PlanCatalogItem(
              "premium-yearly",
              "Premium Anual",
              "Mesmo acesso premium sem anuncios com melhor previsibilidade no ciclo anual.",
              "YEARLY",
              true,
              new BigDecimal("299.90"),
              "BRL",
              List.of(
                  "Sem anuncios",
                  "10 acoes de IA por dia",
                  "Historico emocional completo",
                  "Trilhas personalizadas e premium",
                  "Melhor custo anual"),
              true));

  public List<PlanCatalogItem> listActivePlans() {
    return PLANS.stream().filter(item -> Boolean.TRUE.equals(item.active())).toList();
  }

  public PlanCatalogItem findRequired(String planCode) {
    return listActivePlans().stream()
        .filter(item -> item.planCode().equalsIgnoreCase(planCode))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown planCode"));
  }
}
