package com.evolua.ai.application;

import com.evolua.ai.config.AiProperties;
import com.evolua.ai.infrastructure.security.AuthenticatedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.Normalizer;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class JourneyChatService {
  private static final Logger log = LoggerFactory.getLogger(JourneyChatService.class);

  private final AiProperties aiProperties;
  private final ContentCatalogClient contentCatalogClient;
  private final EmotionalContextClient emotionalContextClient;
  private final SubscriptionQuotaClient subscriptionQuotaClient;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public JourneyChatService(
      AiProperties aiProperties,
      ContentCatalogClient contentCatalogClient,
      EmotionalContextClient emotionalContextClient,
      SubscriptionQuotaClient subscriptionQuotaClient,
      ObjectMapper objectMapper) {
    this.aiProperties = aiProperties;
    this.contentCatalogClient = contentCatalogClient;
    this.emotionalContextClient = emotionalContextClient;
    this.subscriptionQuotaClient = subscriptionQuotaClient;
    this.objectMapper = objectMapper;

    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()));
    requestFactory.setReadTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()));

    this.restClient =
        RestClient.builder()
            .baseUrl(aiProperties.getBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .requestFactory(requestFactory)
            .build();
  }

  @SuppressWarnings("unchecked")
  public JourneyChatResponse reply(
      String authorizationHeader,
      AuthenticatedUser currentUser,
      String message,
      List<JourneyChatMessage> conversationHistory,
      Long trailId) {
    var normalizedMessage = message == null ? "" : message.trim();
    if (normalizedMessage.isBlank()) {
      throw new IllegalArgumentException("message must not be blank");
    }

    var riskLevel = classifyRisk(normalizedMessage);
    if ("high".equals(riskLevel)) {
      return new JourneyChatResponse(
          "Sinto muito que isso esteja tao intenso agora. Antes de aprofundar a jornada, procure apoio humano imediato, fale com alguem de confianca e, se houver risco de se machucar, acione um servico de emergencia da sua regiao.",
          riskLevel,
          "Pausar a jornada e buscar suporte humano agora.",
          false);
    }

    var currentJourney = contentCatalogClient.fetchCurrentJourney(authorizationHeader);
    var emotionalContext = emotionalContextClient.fetchRecentContext(authorizationHeader);
    if (isBlank(aiProperties.getApiKey()) || isBlank(aiProperties.getModel())) {
      return fallbackReply(normalizedMessage, currentJourney, emotionalContext, riskLevel, true);
    }

    var quota = subscriptionQuotaClient.consume(currentUser.userId());
    if (!Boolean.TRUE.equals(quota.allowed())) {
      return fallbackReply(normalizedMessage, currentJourney, emotionalContext, riskLevel, true)
          .withQuotaMetadata(quota, true);
    }

    try {
      Map<String, Object> payload =
          restClient
              .post()
              .uri("/responses")
              .body(
                  buildRequest(
                      normalizedMessage,
                      conversationHistory,
                      currentJourney,
                      emotionalContext,
                      trailId,
                      quota))
              .retrieve()
              .body(Map.class);

      var structured = parseStructuredPayload(payload);
      var reply = stringValue(structured.get("reply"));
      var responseRisk = normalizeRisk(stringValue(structured.get("riskLevel")), riskLevel);
      var suggestedNextStep = stringValue(structured.get("suggestedNextStep"));
      if (reply.isBlank() || suggestedNextStep.isBlank()) {
        return fallbackReply(normalizedMessage, currentJourney, emotionalContext, riskLevel, true);
      }
      return new JourneyChatResponse(reply, responseRisk, suggestedNextStep, false)
          .withQuotaMetadata(quota, false);
    } catch (Exception exception) {
      logAiFallback(exception);
      return fallbackReply(normalizedMessage, currentJourney, emotionalContext, riskLevel, true)
          .withQuotaMetadata(quota, false);
    }
  }

  private Map<String, Object> buildRequest(
      String message,
      List<JourneyChatMessage> conversationHistory,
      JourneyTrailSnapshot currentJourney,
      EmotionalContextSnapshot emotionalContext,
      Long trailId,
      AiQuotaDecision quota) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("model", aiProperties.getModel());
    payload.put("temperature", aiProperties.getTemperature());
    payload.put(
        "max_output_tokens",
        Boolean.TRUE.equals(quota.premium())
            ? Math.min(aiProperties.getMaxTokens() == null ? 900 : aiProperties.getMaxTokens(), 900)
            : Math.min(aiProperties.getMaxTokens() == null ? 450 : aiProperties.getMaxTokens(), 450));
    payload.put(
        "input",
        List.of(
            Map.of("role", "system", "content", buildTextContent(buildSystemPrompt())),
            Map.of(
                "role",
                "user",
                "content",
                buildTextContent(
                    buildUserPrompt(
                        message, conversationHistory, currentJourney, emotionalContext, trailId)))));
    payload.put("text", Map.of("format", buildResponseFormat()));
    return payload;
  }

  private List<Map<String, String>> buildTextContent(String text) {
    return List.of(Map.of("type", "input_text", "text", text));
  }

  private Map<String, Object> buildResponseFormat() {
    return Map.of(
        "type",
        "json_schema",
        "name",
        "journey_chat_reply",
        "strict",
        true,
        "schema",
        Map.of(
            "type",
            "object",
            "additionalProperties",
            false,
            "properties",
            Map.of(
                "reply", Map.of("type", "string"),
                "riskLevel", Map.of("type", "string", "enum", List.of("low", "medium", "high")),
                "suggestedNextStep", Map.of("type", "string")),
            "required",
            List.of("reply", "riskLevel", "suggestedNextStep")));
  }

  private String buildSystemPrompt() {
    return """
        Voce e a conversa de apoio da jornada privada da Evolua.
        Responda sempre em %s.
        Use a trilha atual como contexto principal e aja como apoio psicoeducativo de autocuidado, nao como terapeuta, diagnostico ou prescricao clinica.
        Seja acolhedor e fiel ao que a pessoa acabou de escrever.
        Baseie sua resposta em 2 ou 3 ancoras concretas sempre que possivel, nesta ordem: mensagem atual, ultimo check-in, padrao emocional recente e etapa da jornada.
        Cite 1 ou 2 pontos concretos da mensagem atual quando isso ajudar, sem copiar blocos longos e sem soar mecanico.
        Evite respostas macro, vagas ou que poderiam servir para qualquer pessoa quando houver contexto suficiente.
        Responda em 2 a 4 paragrafos curtos, com tom humano, claro e aplicavel.
        Sempre termine com um proximo passo pratico pequeno.
        Nao cite literalmente Biblia, Neville Goddard, William Blake ou filosofos por padrao; use esses referenciais apenas como lente de sentido, imaginacao, responsabilidade e linguagem simbolica.
        Em risco alto, priorize seguranca, apoio humano e reducao de carga.
        """
        .formatted(aiProperties.getLanguage());
  }

  private String buildUserPrompt(
      String message,
      List<JourneyChatMessage> conversationHistory,
      JourneyTrailSnapshot currentJourney,
      EmotionalContextSnapshot emotionalContext,
      Long trailId) {
    try {
      var payload =
          buildPromptContext(
              message, conversationHistory, currentJourney, emotionalContext, trailId);
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Nao foi possivel serializar o contexto da conversa.", exception);
    }
  }

  Map<String, Object> buildPromptContext(
      String message,
      List<JourneyChatMessage> conversationHistory,
      JourneyTrailSnapshot currentJourney,
      EmotionalContextSnapshot emotionalContext,
      Long trailId) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("mensagemUsuario", message);
    payload.put("trailIdSolicitado", trailId);
    payload.put("historicoCurto", normalizeHistory(conversationHistory));
    payload.put(
        "contextoEmocionalRecente",
        emotionalContext == null ? null : buildEmotionalPayload(emotionalContext));
    payload.put("jornadaAtual", currentJourney == null ? null : buildJourneyPayload(currentJourney));
    payload.put(
        "diretivasDeEstilo",
        Map.of(
            "voice", "acolhedora e fiel",
            "mentionConcreteDetails", true,
            "avoidGenericAdvice", true,
            "precedence",
                List.of(
                    "mensagemUsuario",
                    "contextoEmocionalRecente.ultimoCheckIn",
                    "contextoEmocionalRecente.padraoRecente",
                    "jornadaAtual.secoesEssenciais")));
    return payload;
  }

  private List<Map<String, String>> normalizeHistory(List<JourneyChatMessage> history) {
    if (history == null || history.isEmpty()) {
      return List.of();
    }
    return history.stream()
        .filter(item -> item != null && !isBlank(item.content()))
        .skip(Math.max(0, history.size() - 6L))
        .map(
            item ->
                Map.of(
                    "role", "assistant".equalsIgnoreCase(item.role()) ? "assistant" : "user",
                    "content", truncate(item.content(), 700)))
        .toList();
  }

  private Map<String, Object> buildJourneyPayload(JourneyTrailSnapshot journey) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("id", journey.id());
    payload.put("title", journey.title());
    payload.put("summary", journey.summary());
    payload.put("category", journey.category());
    payload.put("sourceStyle", journey.sourceStyle());
    payload.put("contentExcerpt", truncate(journey.content(), 1800));
    payload.put("secoesEssenciais", JourneyMarkdownSections.extract(journey.content()));
    payload.put("mediaLinks", journey.mediaLinks());
    return payload;
  }

  private Map<String, Object> buildEmotionalPayload(EmotionalContextSnapshot context) {
    var payload = new LinkedHashMap<String, Object>();
    var recentCheckIns = context.recentCheckIns() == null ? List.<RecentCheckInSnapshot>of() : context.recentCheckIns();
    var recentPayload =
        recentCheckIns.stream()
            .limit(3)
            .map(this::buildCheckInPayload)
            .toList();
    payload.put("ultimoCheckIn", recentPayload.isEmpty() ? null : recentPayload.getFirst());
    payload.put("checkInsRecentes", recentPayload);
    var patternPayload = new LinkedHashMap<String, Object>();
    patternPayload.put("averageEnergy", context.averageEnergy());
    patternPayload.put("energyTrendLabel", safeString(context.energyTrendLabel()));
    patternPayload.put("dominantMood", safeString(context.dominantMood()));
    payload.put("padraoRecente", patternPayload);
    return payload;
  }

  private Map<String, Object> buildCheckInPayload(RecentCheckInSnapshot item) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("mood", safeString(item.mood()));
    payload.put("reflection", truncate(safeString(item.reflection()), 280));
    payload.put("energyLevel", item.energyLevel());
    payload.put("recommendedPractice", truncate(safeString(item.recommendedPractice()), 160));
    payload.put("createdAt", item.createdAt() == null ? "" : item.createdAt().toString());
    return payload;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parseStructuredPayload(Map<String, Object> payload)
      throws JsonProcessingException {
    var jsonText = stringValue(payload == null ? null : payload.get("output_text"));
    if (!jsonText.isBlank()) {
      return objectMapper.readValue(jsonText, Map.class);
    }
    if (payload == null) {
      return Map.of();
    }
    var output = payload.get("output") instanceof List<?> rawOutput ? rawOutput : List.of();
    for (var item : output) {
      if (!(item instanceof Map<?, ?> outputMap)) {
        continue;
      }
      var content = outputMap.get("content") instanceof List<?> rawContent ? rawContent : List.of();
      for (var contentItem : content) {
        if (contentItem instanceof Map<?, ?> contentMap) {
          var text = stringValue(contentMap.get("text"));
          if (!text.isBlank()) {
            return objectMapper.readValue(text, Map.class);
          }
        }
      }
    }
    return Map.of();
  }

  private JourneyChatResponse fallbackReply(
      String message,
      JourneyTrailSnapshot currentJourney,
      EmotionalContextSnapshot emotionalContext,
      String riskLevel,
      boolean fallbackUsed) {
    var normalized = normalize(message);
    var hasJourney = currentJourney != null && !isBlank(currentJourney.title());
    var journeyTitle = hasJourney ? currentJourney.title() : "";
    var sections = currentJourney == null ? Map.<String, String>of() : JourneyMarkdownSections.extract(currentJourney.content());
    var direction = truncate(sections.getOrDefault("Direcao da jornada", ""), 180);
    var lastCheckIn = emotionalContext == null || emotionalContext.recentCheckIns() == null || emotionalContext.recentCheckIns().isEmpty()
        ? null
        : emotionalContext.recentCheckIns().getFirst();
    var recentReflection = lastCheckIn == null || isBlank(lastCheckIn.reflection()) ? "" : lastCheckIn.reflection().trim();

    if ("medium".equals(riskLevel)) {
      return new JourneyChatResponse(
          "Vamos reduzir a intensidade antes de tentar entender tudo. Coloque os pes no chao, solte os ombros e faca tres respiracoes mais lentas do que o normal.\n\nDepois disso, me diga o que esta mais forte agora: medo no corpo, pressa nos pensamentos ou vontade de fugir da situacao.",
          riskLevel,
          "Faca tres respiracoes lentas e nomeie a sensacao mais forte no corpo.",
          fallbackUsed);
    }

    if (isMeditationRequest(normalized)) {
      var contextLine =
          recentReflection.isBlank()
              ? "Como voce pediu algo para agora, eu escolheria uma pratica curta, simples e com pouca exigencia."
              : "Como seu contexto recente fala de " + summarizeDetail(recentReflection) + ", eu escolheria uma pratica curta e bem aterrada.";
      return new JourneyChatResponse(
          contextLine
              + "\n\nExperimente uma meditacao de 3 minutos: sente-se de um jeito confortavel, perceba tres pontos de contato do corpo, acompanhe o ar entrando e saindo, e quando a mente puxar assunto, diga mentalmente: \"agora eu volto\".\n\nQuando terminar, me conte se voce ficou mais calmo, mais inquieto ou apenas um pouco mais presente.",
          riskLevel,
          "Faca 3 minutos de respiracao com atencao nos pontos de contato do corpo.",
          fallbackUsed);
    }

    if (isSadOrIntrusive(normalized)) {
      return new JourneyChatResponse(
          "Sinto que hoje esta pesado, especialmente porque pensamentos intrusivos costumam dar a impressao de urgencia mesmo quando voce nao precisa resolver tudo agora.\n\nVamos separar voce dos pensamentos por um momento: escolha um pensamento que apareceu e complete a frase \"minha mente esta dizendo que...\". Isso ajuda a observar sem obedecer automaticamente.\n\nDepois me diga qual pensamento voltou com mais forca, e eu te ajudo a montar um passo bem pequeno para atravessar os proximos minutos.",
          riskLevel,
          "Use a frase \"minha mente esta dizendo que...\" para observar um pensamento sem agir por impulso.",
          fallbackUsed);
    }

    if (!hasJourney && isConversationOnly(normalized)) {
      return new JourneyChatResponse(
          "Pode ser so conversa, sim. A gente nao precisa transformar tudo em trilha agora.\n\nPara eu te acompanhar melhor, me conta por onde voce quer comecar: algo que aconteceu hoje, uma sensacao no corpo, ou uma preocupacao que ficou repetindo na cabeca?",
          riskLevel,
          "Escolha um ponto de partida: acontecimento, sensacao no corpo ou preocupacao recorrente.",
          fallbackUsed);
    }

    if (isAdaptationRequest(normalized) && hasJourney) {
      var directionLine =
          direction.isBlank()
              ? "A ideia e preservar o sentido da sua jornada sem aumentar a carga."
              : "A direcao da sua jornada aponta para: " + direction + ".";
      return new JourneyChatResponse(
          "Vamos adaptar sem perder o fio da meada. " + directionLine + "\n\nEscolha a menor versao possivel do exercicio: em vez de fazer tudo, faca apenas o primeiro minuto, a primeira pergunta ou a primeira anotacao. O objetivo agora e recuperar movimento, nao desempenho.\n\nSe quiser, me diga qual exercicio voce esta tentando adaptar e quanto tempo real voce tem hoje.",
          riskLevel,
          "Reduza o exercicio para uma primeira acao de 1 minuto.",
          fallbackUsed);
    }

    if (hasJourney) {
      var journeyLine =
          direction.isBlank()
              ? "A sua jornada atual pode servir como mapa, mas o ritmo precisa caber no seu dia."
              : "Para hoje, eu usaria esta direcao da jornada como norte: " + direction + ".";
      return new JourneyChatResponse(
          journeyLine
              + "\n\nEm vez de tentar resolver tudo, escolha uma parte pequena do que voce escreveu e transforme em uma pergunta pratica: \"qual e o proximo gesto possivel?\".\n\nMe diga onde voce sente mais resistencia agora, e eu ajusto o passo com voce.",
          riskLevel,
          "Transforme o momento em uma pergunta: qual e o proximo gesto possivel?",
          fallbackUsed);
    }

    return new JourneyChatResponse(
        "Estou com voce. Pelo que aparece agora, vale comecar clareando o momento antes de escolher qualquer caminho.\n\nMe responda com uma frase simples: o que esta pedindo mais atencao hoje, seu corpo, seus pensamentos ou uma decisao que ficou em aberto?",
        riskLevel,
        "Nomeie o foco principal de agora: corpo, pensamentos ou decisao em aberto.",
        fallbackUsed);
  }

  private void logAiFallback(Exception exception) {
    if (exception instanceof RestClientResponseException responseException) {
      log.warn(
          "Journey chat OpenAI request failed; using fallback. status={} code={} exception={}",
          responseException.getStatusCode().value(),
          extractOpenAiErrorCode(responseException.getResponseBodyAsString()),
          exception.getClass().getSimpleName());
      return;
    }

    log.warn(
        "Journey chat OpenAI request failed; using fallback. exception={}",
        exception.getClass().getSimpleName());
  }

  @SuppressWarnings("unchecked")
  private String extractOpenAiErrorCode(String responseBody) {
    try {
      var payload = objectMapper.readValue(safeString(responseBody), Map.class);
      var error = payload.get("error");
      if (error instanceof Map<?, ?> errorMap) {
        var code = stringValue(errorMap.get("code"));
        if (!code.isBlank()) {
          return code;
        }
        return stringValue(errorMap.get("type"));
      }
    } catch (Exception ignored) {
      return "";
    }
    return "";
  }

  private boolean isMeditationRequest(String normalized) {
    return containsAny(normalized, List.of("meditacao", "meditar", "respiracao", "acalmar agora", "me acalmar"));
  }

  private boolean isSadOrIntrusive(String normalized) {
    return containsAny(normalized, List.of("triste", "pensamento intrusivo", "pensamentos intrusivos", "ruminando", "angustia"));
  }

  private boolean isConversationOnly(String normalized) {
    return containsAny(normalized, List.of("nao iniciei", "sem jornada", "batendo um papo", "so conversar", "so conversa", "apenas conversar"));
  }

  private boolean isAdaptationRequest(String normalized) {
    return containsAny(normalized, List.of("adaptar", "adaptacao", "exercicio", "trilha", "pouco tempo", "travei"));
  }

  private String classifyRisk(String value) {
    var text = normalize(value);
    if (containsAny(text, List.of("suic", "morrer", "me matar", "me machucar", "sem saida"))) {
      return "high";
    }
    if (containsAny(text, List.of("panico", "desesper", "crise", "colapso"))) {
      return "medium";
    }
    return "low";
  }

  private String normalizeRisk(String riskLevel, String fallbackValue) {
    var normalized = riskLevel == null ? "" : riskLevel.toLowerCase(Locale.ROOT).trim();
    return switch (normalized) {
      case "low", "medium", "high" -> normalized;
      default -> fallbackValue;
    };
  }

  private boolean containsAny(String source, List<String> tokens) {
    return tokens.stream().anyMatch(source::contains);
  }

  private String truncate(String value, int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value == null ? "" : value;
    }
    return value.substring(0, maxLength);
  }

  private String safeString(String value) {
    return value == null ? "" : value;
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString().trim();
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String normalize(String value) {
    var normalized = Normalizer.normalize(safeString(value), Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
  }

  private String summarizeDetail(String message) {
    if (message == null || message.isBlank()) {
      return "algo importante do seu momento";
    }
    var cleaned = message.trim().replaceAll("\\s+", " ");
    if (cleaned.length() <= 80) {
      return cleaned;
    }
    var truncated = cleaned.substring(0, 80);
    var lastSpace = truncated.lastIndexOf(' ');
    if (lastSpace > 20) {
      truncated = truncated.substring(0, lastSpace);
    }
    return truncated + "...";
  }
}
