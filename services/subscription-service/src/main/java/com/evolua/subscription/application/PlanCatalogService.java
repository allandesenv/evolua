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
              "Base gratuita para check-ins, jornada atual e exploracao do app no seu ritmo.",
              "MONTHLY",
              false,
              BigDecimal.ZERO,
              "BRL",
              List.of(
                  "Check-in e jornada atual",
                  "Reflexoes, espacos e perfil",
                  "Exploracao essencial do app"),
              true),
          new PlanCatalogItem(
              "premium-monthly",
              "Premium Mensal",
              "Aprofunda a jornada com acesso premium e mais continuidade.",
              "MONTHLY",
              true,
              new BigDecimal("29.90"),
              "BRL",
              List.of(
                  "Trilhas premium e conteudo completo",
                  "Mais profundidade na jornada guiada",
                  "Experiencia ampliada de acompanhamento"),
              true),
          new PlanCatalogItem(
              "premium-yearly",
              "Premium Anual",
              "Mesmo acesso premium com melhor previsibilidade no ciclo anual.",
              "YEARLY",
              true,
              new BigDecimal("299.90"),
              "BRL",
              List.of(
                  "Trilhas premium e conteudo completo",
                  "Jornada premium por 12 meses",
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
