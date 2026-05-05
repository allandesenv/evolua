package com.evolua.emotional.application;

import com.evolua.emotional.domain.CheckIn;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CheckinRuleEvaluator {
  private final EmotionalStateCatalog catalog;
  private final boolean aiChatEnabled;
  private final boolean historyAnalysisEnabled;

  public CheckinRuleEvaluator(
      EmotionalStateCatalog catalog,
      @Value("${app.ai.chat-enabled:true}") boolean aiChatEnabled,
      @Value("${app.ai.history-analysis-enabled:true}") boolean historyAnalysisEnabled) {
    this.catalog = catalog;
    this.aiChatEnabled = aiChatEnabled;
    this.historyAnalysisEnabled = historyAnalysisEnabled;
  }

  public CheckInDecision evaluate(CheckInDecisionInput input, List<CheckIn> recentHistory) {
    var definition = catalog.find(input.emotion());
    var intensity = clamp(input.intensity(), 5);
    var energy = normalizeEnergy(input.energy(), input.energyLevel());
    var note = EmotionalStateCatalog.normalize(input.note());
    var severity = severity(definition, intensity, energy, note);
    var repeatedNegative = repeatedNegative(definition, recentHistory);
    var repeatedPositive = repeatedPositive(definition, recentHistory);
    var tags = new ArrayList<String>(definition.tags());
    tags.add("energia-" + energy);
    tags.add("intensidade-" + intensity);
    if (input.context() != null && !input.context().isBlank()) {
      tags.add("contexto-" + EmotionalStateCatalog.normalize(input.context()).replace(' ', '-'));
    }
    if (repeatedNegative || repeatedPositive) {
      tags.add("historico-recorrente");
    }

    var label = definition.label();
    var insight = definition.insight();
    var nextStep = definition.nextStep();
    var trail = new CheckInSuggestedTrail(definition.trailId(), definition.trailTitle());
    var action =
        new CheckInSuggestedAction(
            definition.actionType(), definition.actionTitle(), definition.actionDurationMinutes());

    if (isAnxiety(definition) && intensity >= 7 && "alta".equals(energy)) {
      label = "mente acelerada";
      insight = "Sua mente parece estar tentando resolver muitas coisas ao mesmo tempo.";
      nextStep = "Escolha uma única prioridade para os próximos 10 minutos.";
      trail = new CheckInSuggestedTrail("desacelerar-organizar", "Desacelerar e organizar");
      action = new CheckInSuggestedAction("breathing_or_priority", "Respire e escolha uma prioridade", 3);
      tags.add("organizacao");
    } else if (isSadness(definition) && intensity >= 7 && "baixa".equals(energy)) {
      label = "recolhimento emocional";
      insight = "Hoje talvez você precise de acolhimento antes de tentar resolver tudo.";
      nextStep = "Faça uma ação pequena de cuidado, como beber água, tomar banho ou mandar mensagem para alguém seguro.";
      trail = new CheckInSuggestedTrail("acolhimento-presenca", "Acolhimento e presença");
      action = new CheckInSuggestedAction("microcare", "Microcuidado", 5);
    } else if (isIrritation(definition) && intensity >= 6 && "alta".equals(energy)) {
      label = "tensão ativa";
      insight = "Existe energia disponível, mas ela pode estar buscando uma saída rápida.";
      nextStep = "Antes de responder ou decidir, faça uma pausa curta e nomeie o que realmente incomodou.";
      trail = new CheckInSuggestedTrail("pausa-antes-reacao", "Pausa antes da reação");
      action = new CheckInSuggestedAction("pause_90s", "Pausa de 90 segundos", 2);
    } else if (isFatigue(definition) && "baixa".equals(energy)) {
      label = "baixa energia";
      insight = "Seu corpo pode estar pedindo menos cobrança e mais recuperação.";
      nextStep = "Reduza a meta do momento para algo mínimo e possível.";
      trail = new CheckInSuggestedTrail("recuperar-energia", "Recuperar energia");
      action = new CheckInSuggestedAction("intentional_rest", "Descanso intencional", 10);
    } else if (definition.positive()) {
      label = "estado expansivo";
      insight = "Esse é um bom momento para registrar o que está funcionando.";
      nextStep = "Anote uma escolha ou atitude que contribuiu para esse estado.";
      trail = new CheckInSuggestedTrail("fortalecer-bons-estados", "Fortalecer bons estados");
      action = new CheckInSuggestedAction("gratitude_note", "Registro de gratidão e clareza", 3);
    }

    if (repeatedNegative) {
      if (isAnxiety(definition) || "sobrecarregado".equals(definition.emotion())) {
        trail = new CheckInSuggestedTrail("desacelerar-organizar", "Desacelerar e organizar");
      } else if (isSadness(definition) || "solitário".equals(definition.emotion())) {
        trail = new CheckInSuggestedTrail("acolhimento-conexao", "Acolhimento e conexão");
      }
      insight += " Percebi uma repetição nos últimos dias. Talvez valha olhar esse padrão com mais calma.";
    } else if (repeatedPositive) {
      trail = new CheckInSuggestedTrail("fortalecer-bons-estados", "Fortalecer bons estados");
    }

    if ("critical".equals(severity)) {
      label = "momento de cuidado";
      insight =
          "Seu registro merece apoio com presença. Se existir risco de se machucar ou de não conseguir ficar seguro, procure agora uma pessoa confiável, um profissional ou um serviço de emergência.";
      nextStep = "Priorize segurança e apoio humano antes de tentar resolver qualquer outra coisa.";
      trail = new CheckInSuggestedTrail("seguranca-e-presenca", "Segurança e presença");
      action = new CheckInSuggestedAction("seek_support", "Buscar apoio seguro", 1);
    }

    var shouldSuggestAIChat =
        aiChatEnabled
            && !"critical".equals(severity)
            && (("high".equals(severity) || (definition.negative() && intensity >= 7))
                && !isIrritation(definition));
    var shouldSuggestHistoryAnalysis = historyAnalysisEnabled && (repeatedNegative || repeatedPositive);

    return new CheckInDecision(
        label,
        insight,
        nextStep,
        trail,
        action,
        severity,
        tags.stream().distinct().toList(),
        shouldSuggestAIChat,
        shouldSuggestHistoryAnalysis);
  }

  static String normalizeEnergy(String energy, Integer energyLevel) {
    var normalized = EmotionalStateCatalog.normalize(energy);
    if (List.of("baixa", "media", "alta").contains(normalized)) {
      return "media".equals(normalized) ? "média" : normalized;
    }
    if (energyLevel == null) {
      return "média";
    }
    if (energyLevel <= 3) {
      return "baixa";
    }
    if (energyLevel >= 8) {
      return "alta";
    }
    return "média";
  }

  private String severity(EmotionalStateDefinition definition, int intensity, String energy, String note) {
    if (containsAny(note, List.of("suic", "morrer", "me machucar", "sem saida", "sem saída"))) {
      return "critical";
    }
    if (definition.negative() && intensity >= 8) {
      return "high";
    }
    if (definition.negative() && (intensity >= 6 || "alta".equals(energy) || "baixa".equals(energy))) {
      return "medium";
    }
    return "low";
  }

  private boolean repeatedNegative(EmotionalStateDefinition definition, List<CheckIn> recentHistory) {
    return definition.negative() && repeated(definition, recentHistory);
  }

  private boolean repeatedPositive(EmotionalStateDefinition definition, List<CheckIn> recentHistory) {
    return definition.positive() && repeated(definition, recentHistory);
  }

  private boolean repeated(EmotionalStateDefinition definition, List<CheckIn> recentHistory) {
    var current = EmotionalStateCatalog.normalize(definition.emotion());
    var count =
        recentHistory.stream()
            .map(item -> item.emotion() == null || item.emotion().isBlank() ? item.mood() : item.emotion())
            .map(EmotionalStateCatalog::normalize)
            .filter(current::equals)
            .count();
    return count >= 2;
  }

  private boolean isAnxiety(EmotionalStateDefinition definition) {
    return List.of("ansioso", "acelerado", "sobrecarregado").contains(definition.emotion());
  }

  private boolean isSadness(EmotionalStateDefinition definition) {
    return List.of("triste", "desanimado", "solitário").contains(definition.emotion());
  }

  private boolean isIrritation(EmotionalStateDefinition definition) {
    return List.of("irritado", "frustrado").contains(definition.emotion());
  }

  private boolean isFatigue(EmotionalStateDefinition definition) {
    return List.of("cansado", "sem energia", "desanimado").contains(definition.emotion());
  }

  private int clamp(Integer value, int fallback) {
    if (value == null) {
      return fallback;
    }
    return Math.max(1, Math.min(10, value));
  }

  private boolean containsAny(String source, List<String> tokens) {
    return tokens.stream().anyMatch(source::contains);
  }
}
