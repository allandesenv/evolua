package com.evolua.ai.application;

import com.evolua.ai.config.AiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class JourneyChatService {
  private final AiProperties aiProperties;
  private final ContentCatalogClient contentCatalogClient;
  private final EmotionalContextClient emotionalContextClient;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public JourneyChatService(
      AiProperties aiProperties,
      ContentCatalogClient contentCatalogClient,
      EmotionalContextClient emotionalContextClient,
      ObjectMapper objectMapper) {
    this.aiProperties = aiProperties;
    this.contentCatalogClient = contentCatalogClient;
    this.emotionalContextClient = emotionalContextClient;
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
                      trailId))
              .retrieve()
              .body(Map.class);

      var structured = parseStructuredPayload(payload);
      var reply = stringValue(structured.get("reply"));
      var responseRisk = normalizeRisk(stringValue(structured.get("riskLevel")), riskLevel);
      var suggestedNextStep = stringValue(structured.get("suggestedNextStep"));
      if (reply.isBlank() || suggestedNextStep.isBlank()) {
        return fallbackReply(normalizedMessage, currentJourney, emotionalContext, riskLevel, true);
      }
      return new JourneyChatResponse(reply, responseRisk, suggestedNextStep, false);
    } catch (Exception exception) {
      return fallbackReply(normalizedMessage, currentJourney, emotionalContext, riskLevel, true);
    }
  }

  private Map<String, Object> buildRequest(
      String message,
      List<JourneyChatMessage> conversationHistory,
      JourneyTrailSnapshot currentJourney,
      EmotionalContextSnapshot emotionalContext,
      Long trailId) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("model", aiProperties.getModel());
    payload.put("temperature", aiProperties.getTemperature());
    payload.put("max_output_tokens", Math.min(aiProperties.getMaxTokens() == null ? 700 : aiProperties.getMaxTokens(), 900));
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
    var journeyTitle = currentJourney == null || isBlank(currentJourney.title()) ? "sua jornada atual" : currentJourney.title();
    var sections = currentJourney == null ? Map.<String, String>of() : JourneyMarkdownSections.extract(currentJourney.content());
    var direction = truncate(sections.getOrDefault("Direcao da jornada", ""), 180);
    var lastCheckIn = emotionalContext == null || emotionalContext.recentCheckIns() == null || emotionalContext.recentCheckIns().isEmpty()
        ? null
        : emotionalContext.recentCheckIns().getFirst();
    var recentReflection =
        lastCheckIn == null || isBlank(lastCheckIn.reflection()) ? "" : narrativeDetail(lastCheckIn.reflection());
    var detail = narrativeDetail(message);
    var pattern =
        emotionalContext == null
            ? ""
            : " Seu padrao recente mostra energia em torno de "
                + safeString(emotionalContext.averageEnergy() == null ? "" : emotionalContext.averageEnergy().toString())
                + " e um ritmo "
                + safeString(emotionalContext.energyTrendLabel())
                + ".";
    var reply =
        "Voce trouxe "
            + detail
            + (recentReflection.isBlank()
                ? ", entao vou te responder pelo caminho mais seguro e util agora: conecte isso com "
                : ", e isso conversa com o que apareceu no seu check-in recente sobre " + recentReflection + ". Conecte isso com ")
            + journeyTitle
            + " e escolha uma acao pequena antes de tentar resolver tudo."
            + pattern
            + (direction.isBlank() ? "" : " A direcao mais util agora e: " + direction + ".")
            + "\n\n"
            + "Se quiser continuar, me diga o que ficou mais dificil neste passo e eu ajudo a transformar isso em um movimento mais simples, com base no que voce realmente viveu hoje.";
    return new JourneyChatResponse(reply, riskLevel, "Escolha um exercicio da jornada e pratique por 10 minutos.", fallbackUsed);
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
    return safeString(value).toLowerCase(Locale.ROOT);
  }

  private String narrativeDetail(String message) {
    if (message == null || message.isBlank()) {
      return "algo importante do seu momento";
    }
    var cleaned = message.trim().replaceAll("\\s+", " ");
    if (cleaned.length() <= 80) {
      return "\"" + cleaned + "\"";
    }
    var truncated = cleaned.substring(0, 80);
    var lastSpace = truncated.lastIndexOf(' ');
    if (lastSpace > 20) {
      truncated = truncated.substring(0, lastSpace);
    }
    return "\"" + truncated + "...\"";
  }
}
