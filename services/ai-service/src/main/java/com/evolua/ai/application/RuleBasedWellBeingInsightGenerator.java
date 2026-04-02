package com.evolua.ai.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedWellBeingInsightGenerator implements WellBeingInsightGenerator {
  @Override
  public CheckInInsight generate(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      List<TrailCandidate> candidates,
      List<String> roles) {
    var riskLevel = classifyRisk(currentCheckIn);
    var hasFreeText = hasMeaningfulReflection(currentCheckIn);
    if ("high".equals(riskLevel)) {
      return new CheckInInsight(
          "Seu check-in mostra sinais de sobrecarga importante. Antes de qualquer trilha, vale buscar apoio humano e reduzir a exigencia agora.",
          "Pausa curta, contato com alguem de confianca e suporte profissional se a intensidade continuar.",
          riskLevel,
          null,
          null,
          "Priorize seguranca e apoio direto antes de retomar atividades estruturadas.",
          false);
    }

    var desiredKeywords = desiredKeywords(currentCheckIn);
    var chosenTrail =
        candidates.stream()
            .filter(item -> Boolean.TRUE.equals(item.accessible()))
            .max(
                Comparator.comparingInt(
                    item -> scoreTrail(item, desiredKeywords, Boolean.TRUE.equals(item.premium()))))
            .orElse(null);

    return new CheckInInsight(
        buildInsight(currentCheckIn, context, riskLevel, hasFreeText),
        buildSuggestedAction(currentCheckIn, chosenTrail != null, hasFreeText, riskLevel),
        riskLevel,
        chosenTrail == null ? null : chosenTrail.id(),
        chosenTrail == null ? null : chosenTrail.title(),
        buildTrailReason(chosenTrail, context, riskLevel, hasFreeText),
        false);
  }

  private String classifyRisk(CurrentCheckInInput currentCheckIn) {
    var text = normalize(currentCheckIn.mood()) + " " + normalize(currentCheckIn.reflection());
    if (containsAny(text, List.of("suic", "morrer", "matar", "desist", "sem saida", "me machucar"))) {
      return "high";
    }
    if (currentCheckIn.energyLevel() != null
        && currentCheckIn.energyLevel() <= 2
        && containsAny(text, List.of("vazio", "exaust", "sem energia", "travado"))) {
      return "medium";
    }
    if (containsAny(text, List.of("ansios", "panico", "sobrecarga", "pressionad", "culpa"))) {
      return "medium";
    }
    return "low";
  }

  private List<String> desiredKeywords(CurrentCheckInInput currentCheckIn) {
    var text = normalize(currentCheckIn.mood()) + " " + normalize(currentCheckIn.reflection());
    var keywords = new ArrayList<String>();
    if (containsAny(text, List.of("ansios", "agitado", "sobrecarga", "panico"))) {
      keywords.addAll(List.of("ansiedade", "calma", "respir", "sono", "pausa"));
    }
    if (containsAny(text, List.of("cans", "exaust", "sem energia", "sono"))
        || (currentCheckIn.energyLevel() != null && currentCheckIn.energyLevel() <= 4)) {
      keywords.addAll(List.of("sono", "ritmo", "energia", "pausa", "respir"));
    }
    if (containsAny(text, List.of("calmo", "presente", "foco", "clareza"))
        || (currentCheckIn.energyLevel() != null && currentCheckIn.energyLevel() >= 6)) {
      keywords.addAll(List.of("foco", "clareza", "constancia", "rotina", "presenca"));
    }
    if (keywords.isEmpty()) {
      keywords.addAll(List.of("ritmo", "presenca", "foco"));
    }
    return keywords;
  }

  private int scoreTrail(TrailCandidate trail, List<String> keywords, boolean premium) {
    var haystack =
        normalize(trail.title()) + " " + normalize(trail.summary()) + " " + normalize(trail.category());
    var score = premium ? -2 : 0;
    for (var keyword : keywords) {
      if (haystack.contains(keyword)) {
        score += 4;
      }
    }
    if (containsAny(haystack, List.of("curta", "leve", "guiada", "gentil"))) {
      score += 1;
    }
    return score;
  }

  private String buildInsight(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      String riskLevel,
      boolean hasFreeText) {
    var mood = normalize(currentCheckIn.mood());
    var trend = context.energyTrendLabel() == null ? "estavel" : context.energyTrendLabel();
    var averageEnergy = context.averageEnergy() == null ? currentCheckIn.energyLevel() : context.averageEnergy();

    if ("medium".equals(riskLevel)) {
      return hasFreeText
          ? "Pelo que voce descreveu, ha sinais de tensao relevante neste momento. O mais util agora e diminuir a carga e escolher uma resposta pequena, reguladora e viavel."
          : "Sem muitos detalhes do motivo, seu check-in e o padrao recente sugerem tensao relevante agora. Vale reduzir a carga e escolher uma proxima acao curta e reguladora.";
    }
    if (mood.contains("ans")) {
      return hasFreeText
          ? "Pelo que voce descreveu, seu sistema parece mais ativado do que disponivel para performance. Seu historico recente indica que regulacao vem antes de produtividade agora."
          : "Sem muitos detalhes do motivo, seu humor atual e o historico recente sugerem ativacao emocional alta. Faz mais sentido regular o corpo e a atencao antes de cobrar rendimento.";
    }
    if (mood.contains("cans")) {
      return hasFreeText
          ? "O contexto que voce trouxe combina com um momento de desgaste real. Seu corpo parece pedir recuperacao e ritmo, entao o melhor proximo passo agora e leve e sustentavel."
          : "Mesmo sem detalhes do motivo, sua energia e o historico recente apontam necessidade de recuperacao. O melhor proximo passo agora e algo leve e sustentavel.";
    }
    if (hasFreeText) {
      return "Pelo que voce descreveu, existe espaco para avancar com mais clareza sem forcar demais. Seu historico recente mostra um ritmo "
          + trend
          + " e energia media em torno de "
          + safeEnergyLabel(averageEnergy)
          + ".";
    }
    return "Sem muitos detalhes do motivo, seu check-in indica uma boa base para seguir com constancia. O padrao recente mostra um ritmo "
        + trend
        + " e energia media em torno de "
        + safeEnergyLabel(averageEnergy)
        + ".";
  }

  private String buildSuggestedAction(
      CurrentCheckInInput currentCheckIn,
      boolean hasTrailSuggestion,
      boolean hasFreeText,
      String riskLevel) {
    if ("medium".equals(riskLevel)) {
      return hasFreeText
          ? "Escolha uma unica resposta de regulacao pelos proximos 5 a 10 minutos e adie decisoes maiores ate sentir mais estabilidade."
          : "Como o contexto ainda esta parcial, comece com uma pratica curta de regulacao antes de decidir o restante do dia.";
    }
    if (currentCheckIn.energyLevel() != null && currentCheckIn.energyLevel() <= 4) {
      return hasFreeText
          ? "Respeite o ritmo que apareceu no seu relato e comece por uma pratica curta de regulacao antes de ampliar qualquer meta."
          : "Sua energia sugere comecar pequeno: 5 minutos de regulacao antes de ampliar qualquer meta.";
    }
    if (normalize(currentCheckIn.mood()).contains("ans")) {
      return hasFreeText
          ? "Comece por respiracao guiada ou pausa sensorial curta para responder melhor ao que voce descreveu, e so depois decida o resto do dia."
          : "Comece por respiracao guiada ou uma pausa sensorial curta, e use esse ajuste para decidir o resto do dia.";
    }
    return hasTrailSuggestion
        ? (hasFreeText
            ? "Use a trilha sugerida como proximo passo unico, porque ela conversa com o contexto que voce trouxe."
            : "Use a trilha sugerida como proximo passo unico, apoiando-se no seu padrao recente.")
        : (hasFreeText
            ? "Transforme o que apareceu no seu relato em uma unica acao leve e executavel ainda hoje."
            : "Mantenha o check-in como ancora e siga com uma unica acao leve.");
  }

  private String buildTrailReason(
      TrailCandidate chosenTrail,
      EmotionalContextSnapshot context,
      String riskLevel,
      boolean hasFreeText) {
    if (chosenTrail == null) {
      return hasFreeText
          ? "Ainda nao apareceu uma trilha suficientemente aderente ao contexto que voce descreveu."
          : "Com o contexto atual, nenhuma trilha apareceu como aderencia forte o bastante para sugerir com seguranca.";
    }

    var trend = context.energyTrendLabel() == null ? "mais estavel" : context.energyTrendLabel();
    if ("medium".equals(riskLevel)) {
      return "Sugeri essa trilha porque ela tende a apoiar regulacao e retomada gradual, sem aumentar a exigencia enquanto seu sistema ainda pede cuidado.";
    }
    if (hasFreeText) {
      return "Sugeri essa trilha porque ela conversa com o contexto que voce descreveu e combina com o seu ritmo recente "
          + trend
          + ".";
    }
    return "Sugeri essa trilha porque, mesmo com contexto parcial, ela combina com seu estado atual e com o padrao recente de energia "
        + trend
        + ".";
  }

  private boolean hasMeaningfulReflection(CurrentCheckInInput currentCheckIn) {
    return currentCheckIn.reflection() != null && !currentCheckIn.reflection().trim().isEmpty();
  }

  private String safeEnergyLabel(Integer averageEnergy) {
    if (averageEnergy == null) {
      return "sem base suficiente";
    }
    return averageEnergy + "/10";
  }

  private boolean containsAny(String source, List<String> tokens) {
    return tokens.stream().anyMatch(source::contains);
  }

  private String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }
}
