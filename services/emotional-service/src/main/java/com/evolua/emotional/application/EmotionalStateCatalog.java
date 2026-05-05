package com.evolua.emotional.application;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmotionalStateCatalog {
  private final Map<String, EmotionalStateDefinition> states =
      Map.ofEntries(
          state("tranquilo", "positiva", "baixa", "presença", "estabilidade tranquila",
              "Seu sistema parece ter encontrado um pouco mais de espaço interno.",
              "Use esse momento para escolher uma ação simples e consciente.",
              "fortalecer-bons-estados", "Fortalecer bons estados", "clarity_note",
              "Registrar o que esta funcionando", 3, List.of("tranquilo", "presenca")),
          state("feliz", "positiva", "média", "expressão", "estado expansivo",
              "Esse é um bom momento para registrar o que está funcionando.",
              "Anote uma escolha ou atitude que contribuiu para esse estado.",
              "fortalecer-bons-estados", "Fortalecer bons estados", "gratitude_note",
              "Registro de gratidao e clareza", 3, List.of("felicidade", "reforco")),
          state("motivado", "positiva", "alta", "ação", "estado expansivo",
              "Há energia disponível para transformar intenção em movimento.",
              "Escolha uma prioridade pequena e comece por ela antes de abrir novas frentes.",
              "fortalecer-bons-estados", "Fortalecer bons estados", "priority_start",
              "Escolher uma prioridade", 5, List.of("motivacao", "acao")),
          state("grato", "positiva", "média", "expressão", "estado expansivo",
              "Esse é um bom momento para registrar o que está funcionando.",
              "Anote uma escolha ou atitude que contribuiu para esse estado.",
              "fortalecer-bons-estados", "Fortalecer bons estados", "gratitude_note",
              "Registro de gratidao e clareza", 3, List.of("gratidao", "reforco")),
          state("cansado", "negativa", "baixa", "descanso", "baixa energia",
              "Seu corpo pode estar pedindo menos cobrança e mais recuperação.",
              "Reduza a meta do momento para algo mínimo e possível.",
              "recuperar-energia", "Recuperar energia", "intentional_rest",
              "Descanso intencional", 10, List.of("cansaco", "recuperacao")),
          state("ansioso", "negativa", "alta", "respiração", "mente acelerada",
              "Sua mente parece estar tentando resolver muitas coisas ao mesmo tempo.",
              "Escolha uma única prioridade para os próximos 10 minutos.",
              "desacelerar-organizar", "Desacelerar e organizar", "breathing_or_priority",
              "Respire e escolha uma prioridade", 3, List.of("ansiedade", "organizacao")),
          state("triste", "negativa", "baixa", "acolhimento", "recolhimento emocional",
              "Hoje talvez você precise de acolhimento antes de tentar resolver tudo.",
              "Faça uma ação pequena de cuidado, como beber água, tomar banho ou mandar mensagem para alguém seguro.",
              "acolhimento-presenca", "Acolhimento e presença", "microcare",
              "Microcuidado", 5, List.of("tristeza", "acolhimento")),
          state("irritado", "negativa", "alta", "pausa", "tensão ativa",
              "Existe energia disponível, mas ela pode estar buscando uma saída rápida.",
              "Antes de responder ou decidir, faça uma pausa curta e nomeie o que realmente incomodou.",
              "pausa-antes-reacao", "Pausa antes da reação", "pause_90s",
              "Pausa de 90 segundos", 2, List.of("irritacao", "pausa")),
          state("sobrecarregado", "negativa", "alta", "organização", "sobrecarga ativa",
              "Parece haver mais demandas do que espaço interno para processar agora.",
              "Tire da cabeça uma lista curta e marque apenas o que precisa de atenção hoje.",
              "desacelerar-organizar", "Desacelerar e organizar", "priority_list",
              "Lista de uma prioridade", 5, List.of("sobrecarga", "organizacao")),
          state("confuso", "neutra", "média", "clareza", "clareza em construção",
              "A confusão pode ser um sinal de que há informações demais misturadas.",
              "Escreva em uma frase o que você sabe e em outra o que ainda precisa descobrir.",
              "clareza-pratica", "Clareza prática", "two_sentences",
              "Separar fatos e dúvidas", 5, List.of("clareza", "organizacao")),
          state("inseguro", "negativa", "média", "acolhimento", "base insegura",
              "Talvez exista uma parte sua pedindo segurança antes de avançar.",
              "Escolha um passo pequeno que não dependa de ter certeza absoluta.",
              "coragem-gentil", "Coragem gentil", "small_courage",
              "Um passo seguro", 5, List.of("inseguranca", "coragem")),
          state("desanimado", "negativa", "baixa", "acolhimento", "energia reduzida",
              "Hoje a motivação pode estar baixa, e isso não precisa virar cobrança.",
              "Faça uma ação mínima que ajude o dia a continuar sem exigir demais.",
              "recuperar-energia", "Recuperar energia", "minimum_action",
              "Ação mínima", 5, List.of("desanimo", "recuperacao")),
          state("solitário", "negativa", "baixa", "conexão", "necessidade de conexão",
              "Seu estado pode estar pedindo presença, vínculo ou contato seguro.",
              "Mande uma mensagem simples para alguém confiável ou fique perto de um ambiente mais acolhedor.",
              "acolhimento-conexao", "Acolhimento e conexão", "safe_message",
              "Mensagem para alguém seguro", 5, List.of("solidao", "conexao")),
          state("esperançoso", "positiva", "média", "ação", "esperança ativa",
              "Existe uma abertura para continuar, mesmo que nem tudo esteja resolvido.",
              "Transforme essa esperança em uma escolha pequena para hoje.",
              "fortalecer-bons-estados", "Fortalecer bons estados", "hope_to_action",
              "Esperança em ação", 5, List.of("esperanca", "acao")),
          state("culpado", "negativa", "média", "expressão", "peso de responsabilidade",
              "A culpa pode estar tentando apontar algo importante, mas não precisa conduzir tudo sozinha.",
              "Separe o que é reparável do que é apenas cobrança repetida.",
              "reparar-sem-cobranca", "Reparar sem cobrança", "repair_or_release",
              "Reparar ou soltar", 7, List.of("culpa", "clareza")),
          state("frustrado", "negativa", "alta", "expressão", "frustração ativa",
              "Algo importante para você parece ter encontrado resistência.",
              "Antes de insistir, ajuste a rota para uma versão menor e possível.",
              "pausa-antes-reacao", "Pausa antes da reação", "route_adjust",
              "Ajustar a rota", 5, List.of("frustracao", "ajuste")),
          state("com medo", "negativa", "alta", "acolhimento", "alerta interno",
              "Seu sistema pode estar tentando proteger você de algo percebido como ameaça.",
              "Volte ao corpo, observe o ambiente e escolha uma ação segura antes de decidir qualquer coisa maior.",
              "seguranca-e-presenca", "Segurança e presença", "grounding",
              "Aterramento breve", 4, List.of("medo", "seguranca")),
          state("em paz", "positiva", "baixa", "presença", "estado de presença",
              "Há um sinal de calma que pode ser protegido com escolhas simples.",
              "Observe o que ajudou você a chegar aqui e preserve um pouco desse ritmo.",
              "fortalecer-bons-estados", "Fortalecer bons estados", "peace_note",
              "Registrar o que trouxe paz", 3, List.of("paz", "presenca")),
          state("sem energia", "negativa", "baixa", "descanso", "baixa energia",
              "Seu corpo pode estar pedindo menos cobrança e mais recuperação.",
              "Reduza a meta do momento para algo mínimo e possível.",
              "recuperar-energia", "Recuperar energia", "intentional_rest",
              "Descanso intencional", 10, List.of("sem-energia", "recuperacao")),
          state("acelerado", "negativa", "alta", "respiração", "mente acelerada",
              "Sua mente parece estar tentando resolver muitas coisas ao mesmo tempo.",
              "Escolha uma única prioridade para os próximos 10 minutos.",
              "desacelerar-organizar", "Desacelerar e organizar", "breathing_or_priority",
              "Respire e escolha uma prioridade", 3, List.of("aceleracao", "respiracao")));

  public EmotionalStateDefinition find(String emotion) {
    return states.getOrDefault(normalize(emotion), states.get("confuso"));
  }

  private static Map.Entry<String, EmotionalStateDefinition> state(
      String emotion,
      String valence,
      String activation,
      String need,
      String label,
      String insight,
      String nextStep,
      String trailId,
      String trailTitle,
      String actionType,
      String actionTitle,
      Integer actionDurationMinutes,
      List<String> tags) {
    return Map.entry(
        normalize(emotion),
        new EmotionalStateDefinition(
            emotion,
            valence,
            activation,
            need,
            label,
            insight,
            nextStep,
            trailId,
            trailTitle,
            actionType,
            actionTitle,
            actionDurationMinutes,
            tags));
  }

  static String normalize(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    var normalized =
        Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
    return normalized.replace('-', ' ').replace('_', ' ').replaceAll("\\s+", " ");
  }
}
