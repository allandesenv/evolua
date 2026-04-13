package com.evolua.ai.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedWellBeingInsightGenerator implements WellBeingInsightGenerator {
  private final CuratedJourneyLinkLibrary linkLibrary;

  public RuleBasedWellBeingInsightGenerator(CuratedJourneyLinkLibrary linkLibrary) {
    this.linkLibrary = linkLibrary;
  }

  @Override
  public CheckInInsight generate(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      List<TrailCandidate> candidates,
      List<SpaceCandidate> spaces,
      List<String> roles) {
    var riskLevel = classifyRisk(currentCheckIn);
    var hasFreeText = hasMeaningfulReflection(currentCheckIn);
    var journeyKey = journeyKey(currentCheckIn);
    var sourceStyle = sourceStyle(journeyKey);
    var suggestedSpace = chooseSpace(spaces, currentCheckIn);

    if ("high".equals(riskLevel)) {
      return new CheckInInsight(
          "Seu check-in mostra sinais de sobrecarga importante. Antes de qualquer aprofundamento, vale buscar apoio humano e reduzir a exigencia agora.",
          "Pausa curta, contato com alguem de confianca e suporte profissional se a intensidade continuar.",
          riskLevel,
          null,
          null,
          "Priorize seguranca e apoio direto antes de retomar atividades estruturadas.",
          suggestedSpace == null
              ? null
              : new SuggestedSpace(
                  suggestedSpace.id(),
                  suggestedSpace.slug(),
                  suggestedSpace.name(),
                  "Se fizer sentido depois de estabilizar o momento, este espaco pode oferecer acolhimento leve sem pressao."),
          new JourneyPlan(
              journeyKey,
              "Voltar ao eixo com seguranca",
              "Estabilizacao",
              "reset",
              "Hoje a prioridade e recuperar seguranca, respiracao e suporte antes de qualquer meta.",
              "No proximo check-in, conte se a intensidade reduziu ou se voce conseguiu acionar apoio."),
          null,
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
    var generatedTrail = buildGeneratedTrailDraft(currentCheckIn, context, journeyKey, sourceStyle);
    var journeyPlan = buildJourneyPlan(currentCheckIn, context, journeyKey);

    return new CheckInInsight(
        buildInsight(currentCheckIn, context, riskLevel, hasFreeText),
        buildSuggestedAction(currentCheckIn, generatedTrail.title(), hasFreeText, riskLevel),
        riskLevel,
        chosenTrail == null ? null : chosenTrail.id(),
        generatedTrail.title(),
        buildTrailReason(chosenTrail, context, riskLevel, hasFreeText, generatedTrail.title()),
        suggestedSpace == null
            ? null
            : new SuggestedSpace(
                suggestedSpace.id(),
                suggestedSpace.slug(),
                suggestedSpace.name(),
                buildSuggestedSpaceReason(suggestedSpace, currentCheckIn)),
        journeyPlan,
        generatedTrail,
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
      CurrentCheckInInput currentCheckIn, String trailTitle, boolean hasFreeText, String riskLevel) {
    if ("medium".equals(riskLevel)) {
      return hasFreeText
          ? "Comece pelos primeiros 10 minutos da jornada proposta e adie decisoes maiores ate sentir mais estabilidade."
          : "Como o contexto ainda esta parcial, comece pelos primeiros 10 minutos da jornada antes de decidir o restante do dia.";
    }
    if (currentCheckIn.energyLevel() != null && currentCheckIn.energyLevel() <= 4) {
      return hasFreeText
          ? "Respeite o ritmo do seu relato e inicie a jornada em modo leve, sem aumentar exigencia hoje."
          : "Sua energia sugere comecar pequeno: inicie a jornada em ritmo leve e observe a resposta do corpo.";
    }
    if (normalize(currentCheckIn.mood()).contains("ans")) {
      return hasFreeText
          ? "Comece pelo primeiro bloco da jornada, com respiracao e ancoragem, e so depois reavalie o restante do dia."
          : "Comece pelo primeiro bloco da jornada com respiracao e ancoragem, e use isso para decidir o resto do dia.";
    }
    return hasFreeText
        ? "Siga pela jornada privada sugerida e transforme o que apareceu no seu relato em uma unica frente clara hoje."
        : "Use a jornada " + trailTitle + " como proximo passo unico, apoiando-se no seu padrao recente.";
  }

  private String buildTrailReason(
      TrailCandidate chosenTrail,
      EmotionalContextSnapshot context,
      String riskLevel,
      boolean hasFreeText,
      String generatedTitle) {
    var trend = context.energyTrendLabel() == null ? "mais estavel" : context.energyTrendLabel();
    if ("medium".equals(riskLevel)) {
      return "A jornada foi desenhada para apoiar regulacao e retomada gradual, sem aumentar a exigencia enquanto seu sistema ainda pede cuidado.";
    }
    if (chosenTrail != null && hasFreeText) {
      return "A trilha privada segue o mesmo eixo de cuidado que ja aparece em \""
          + chosenTrail.title()
          + "\" e conversa com o contexto que voce descreveu.";
    }
    if (hasFreeText) {
      return "A jornada " + generatedTitle + " conversa com o contexto que voce descreveu e combina com o seu ritmo recente " + trend + ".";
    }
    return "A jornada " + generatedTitle + " foi montada para sustentar seu estado atual com mais constancia e menos pressao.";
  }

  private SuggestedSpace chooseSpace(List<SpaceCandidate> spaces, CurrentCheckInInput currentCheckIn) {
    return spaces.stream()
        .filter(item -> !"PRIVATE".equalsIgnoreCase(item.visibility()))
        .max(Comparator.comparingInt(item -> scoreSpace(item, currentCheckIn)))
        .map(item -> new SuggestedSpace(item.id(), item.slug(), item.name(), ""))
        .orElse(null);
  }

  private int scoreSpace(SpaceCandidate item, CurrentCheckInInput currentCheckIn) {
    var haystack =
        normalize(item.name()) + " " + normalize(item.description()) + " " + normalize(item.category());
    var text = normalize(currentCheckIn.mood()) + " " + normalize(currentCheckIn.reflection());
    var score = Boolean.TRUE.equals(item.joined()) ? 2 : 0;
    if (containsAny(text, List.of("ansios", "sobrecarga", "panico")) && containsAny(haystack, List.of("ansiedade", "acolh"))) {
      score += 5;
    }
    if (containsAny(text, List.of("relacion", "conflito", "sozinho")) && containsAny(haystack, List.of("relacion", "vincul", "amor"))) {
      score += 5;
    }
    if (containsAny(text, List.of("proposito", "sentido", "direcao")) && containsAny(haystack, List.of("proposito", "clareza", "vida"))) {
      score += 5;
    }
    if (containsAny(text, List.of("confi", "medo", "insegur")) && containsAny(haystack, List.of("autoconfi", "coragem", "forca"))) {
      score += 5;
    }
    if (score == 0 && containsAny(haystack, List.of("acolh", "calma", "presenca", "clareza"))) {
      score += 2;
    }
    return score;
  }

  private String buildSuggestedSpaceReason(SuggestedSpace space, CurrentCheckInInput currentCheckIn) {
    var text = normalize(currentCheckIn.mood()) + " " + normalize(currentCheckIn.reflection());
    if (containsAny(text, List.of("ansios", "sobrecarga", "panico"))) {
      return "Esse espaco pode reforcar acolhimento, regulacao e relatos de quem tambem esta lidando com ansiedade sem pressao social.";
    }
    if (containsAny(text, List.of("relacion", "conflito", "sozinho"))) {
      return "Esse espaco tende a reunir reflexoes e praticas leves sobre vinculos, limites e conexoes mais conscientes.";
    }
    if (containsAny(text, List.of("proposito", "direcao", "sentido"))) {
      return "Esse espaco pode ajudar a sustentar clareza, significado e pequenos passos alinhados ao que importa agora.";
    }
    return "Esse espaco conversa com o seu momento e pode complementar a jornada com reflexoes leves e praticas aplicaveis.";
  }

  private JourneyPlan buildJourneyPlan(CurrentCheckInInput currentCheckIn, EmotionalContextSnapshot context, String journeyKey) {
    var mood = normalize(currentCheckIn.mood());
    if (journeyKey.contains("ansiedade")) {
      return new JourneyPlan(
          journeyKey,
          "Regulacao e retorno ao eixo",
          "Aterramento",
          "continue",
          "Uma jornada curta para diminuir ativacao, organizar a atencao e recuperar senso de escolha.",
          "No proximo check-in, conte se a ansiedade diminuiu no corpo, nos pensamentos ou no ritmo do dia.");
    }
    if (journeyKey.contains("recuperacao")) {
      return new JourneyPlan(
          journeyKey,
          "Recuperacao gentil de energia",
          "Restauracao",
          "continue",
          "Uma jornada para desacelerar a exigencia, recuperar energia e voltar ao ritmo sem violencia interna.",
          "No proximo check-in, conte se houve mais descanso, mais clareza ou menos esgotamento.");
    }
    if (mood.contains("calm") || mood.contains("presen")) {
      return new JourneyPlan(
          journeyKey,
          "Clareza, foco e constancia",
          "Consolidacao",
          "continue",
          "Uma jornada para transformar estabilidade em direcao pratica e constancia leve.",
          "No proximo check-in, conte o que ajudou a sustentar clareza sem endurecer o ritmo.");
    }
    return new JourneyPlan(
        journeyKey,
        "Reorientacao consciente",
        "Observacao",
        "continue",
        "Uma jornada para entender o momento atual e traduzir isso em passos pequenos e consistentes.",
        "No proximo check-in, conte qual pequeno movimento fez mais sentido para voce.");
  }

  private GeneratedTrailDraft buildGeneratedTrailDraft(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      String journeyKey,
      String sourceStyle) {
    var title = switch (journeyKey) {
      case "ansiedade-regulacao" -> "Voltar ao eixo em dias de ansiedade";
      case "recuperacao-gentil" -> "Recuperacao gentil para corpo e mente";
      case "clareza-e-acao" -> "Clareza pratica para seguir com constancia";
      default -> "Reorientacao consciente para este momento";
    };
    var summary = switch (journeyKey) {
      case "ansiedade-regulacao" -> "Uma trilha privada para reduzir ativacao, recuperar presenca e agir com menos pressa interna.";
      case "recuperacao-gentil" -> "Uma trilha privada para descansar a cobranca, recompor energia e retomar o ritmo com suavidade.";
      case "clareza-e-acao" -> "Uma trilha privada para sustentar foco, significado e acao intencional sem endurecer o corpo.";
      default -> "Uma trilha privada para compreender seu momento e traduzi-lo em passos pequenos e viaveis.";
    };

    var content =
        """
# $title

## Intencao da jornada
$summary

Esta jornada foi curada a partir do seu check-in mais recente e combina uma lente de neurociencia aplicada com reflexao interior e sentido pratico. O objetivo nao e exigir mais de voce, e sim ajudar seu sistema a encontrar um proximo passo regulado, consciente e sustentavel.

## Leitura do momento
Seu estado atual merece ser tratado como informacao, nao como falha. A energia media recente esta em ${safeEnergyLabel(context.averageEnergy())}, a tendencia aparece como ${context.energyTrendLabel() == null ? "inicial" : context.energyTrendLabel()} e o humor dominante recente e ${context.dominantMood() == null || context.dominantMood().isBlank() ? "indefinido" : context.dominantMood()}.

## Direcao da jornada
Esta trilha usa corpo, atencao e significado em uma sequencia simples: primeiro regular, depois observar, depois agir. A ideia e criar uma resposta pequena e repetivel, nao um plano perfeito.

## Conversa guiada
Use estas perguntas como uma conversa consigo mesmo antes de seguir:
- O que meu estado esta tentando proteger ou sinalizar hoje?
- Que parte disso e fato concreto, e que parte e antecipacao ou interpretacao?
- Se eu respondesse com firmeza e gentileza, qual seria o proximo gesto?
- Que pensamento eu posso soltar por algumas horas sem precisar resolver agora?

## Dicas praticas
- Reduza a exigencia antes de aumentar a performance.
- Escolha um ambiente com menos ruido por 10 minutos.
- Transforme uma preocupacao ampla em uma acao fisica pequena.
- Evite comparar seu ritmo atual com o melhor dia de outra pessoa.

## Exercicios
1. Respiracao de retorno: inspire por 4 segundos, expire por 6 segundos e repita por 3 minutos.
2. Escrita breve: complete a frase "Agora eu percebo que..." por 5 linhas, sem editar.
3. Acao minima: escolha uma tarefa que caiba em 10 minutos e termine antes de abrir outra.
4. Fechamento: registre uma frase sobre o que mudou no corpo ou na clareza.

## Plano de 24 horas
1. Reserve de 10 a 15 minutos para iniciar sem interrupcoes.
2. Comece pelo corpo: respiracao mais lenta, ombros soltos e atencao no ambiente.
3. Nomeie em uma frase o que esta mais pesando agora.
4. Escolha apenas uma acao simples para o restante do dia.

## Plano de 7 dias
- Dia 1: estabilizar o corpo e nomear o estado.
- Dia 2: repetir o exercicio mais util por 10 minutos.
- Dia 3: observar um gatilho recorrente sem se julgar.
- Dia 4: escolher uma pequena pratica de reparo ou coragem.
- Dia 5: conversar com alguem seguro ou registrar um insight.
- Dia 6: revisar o que deu certo sem transformar em cobranca.
- Dia 7: fazer novo check-in e ajustar o rumo da jornada.

## Espaco sugerido
Entre no espaco indicado pela IA apenas se isso trouxer acolhimento, nao pressao. O objetivo e encontrar reflexoes e conversas leves, nao competir ou se expor demais.

## Proximos check-ins
- Inicio: 10 a 15 minutos hoje
- Continuidade: revisitar amanha com novo check-in
- Ajuste: deixar a jornada evoluir conforme seu estado real

## Lembrete de seguranca
Esta jornada e apoio de autocuidado e reflexao. Se o sofrimento ficar intenso, persistente ou vier com risco de se machucar, priorize apoio humano imediato e suporte profissional.
""";

    return new GeneratedTrailDraft(
        title, summary, content, normalizeCategory(journeyKey), sourceStyle, linkLibrary.linksFor(journeyKey));
  }

  private String journeyKey(CurrentCheckInInput currentCheckIn) {
    var text = normalize(currentCheckIn.mood()) + " " + normalize(currentCheckIn.reflection());
    if (containsAny(text, List.of("ansios", "panico", "sobrecarga", "culpa"))) {
      return "ansiedade-regulacao";
    }
    if (containsAny(text, List.of("cans", "exaust", "sono", "sem energia"))
        || (currentCheckIn.energyLevel() != null && currentCheckIn.energyLevel() <= 4)) {
      return "recuperacao-gentil";
    }
    if (containsAny(text, List.of("calmo", "presente", "clareza", "foco"))
        || (currentCheckIn.energyLevel() != null && currentCheckIn.energyLevel() >= 7)) {
      return "clareza-e-acao";
    }
    return "reorientacao-consciente";
  }

  private String sourceStyle(String journeyKey) {
    return switch (journeyKey) {
      case "ansiedade-regulacao" -> "neuro-stoic-contemplative";
      case "recuperacao-gentil" -> "neuro-biblical-restorative";
      case "clareza-e-acao" -> "neuro-stoic-visionary";
      default -> "neuro-symbolic-reflective";
    };
  }

  private String normalizeCategory(String journeyKey) {
    return switch (journeyKey) {
      case "ansiedade-regulacao" -> "ansiedade";
      case "recuperacao-gentil" -> "energia";
      case "clareza-e-acao" -> "clareza";
      default -> "autoconhecimento";
    };
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
