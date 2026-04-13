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
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public JourneyChatService(
      AiProperties aiProperties,
      ContentCatalogClient contentCatalogClient,
      ObjectMapper objectMapper) {
    this.aiProperties = aiProperties;
    this.contentCatalogClient = contentCatalogClient;
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
    if (isBlank(aiProperties.getApiKey()) || isBlank(aiProperties.getModel())) {
      return fallbackReply(normalizedMessage, currentJourney, riskLevel, true);
    }

    try {
      Map<String, Object> payload =
          restClient
              .post()
              .uri("/responses")
              .body(buildRequest(normalizedMessage, conversationHistory, currentJourney, trailId))
              .retrieve()
              .body(Map.class);

      var structured = parseStructuredPayload(payload);
      var reply = stringValue(structured.get("reply"));
      var responseRisk = normalizeRisk(stringValue(structured.get("riskLevel")), riskLevel);
      var suggestedNextStep = stringValue(structured.get("suggestedNextStep"));
      if (reply.isBlank() || suggestedNextStep.isBlank()) {
        return fallbackReply(normalizedMessage, currentJourney, riskLevel, true);
      }
      return new JourneyChatResponse(reply, responseRisk, suggestedNextStep, false);
    } catch (Exception exception) {
      return fallbackReply(normalizedMessage, currentJourney, riskLevel, true);
    }
  }

  private Map<String, Object> buildRequest(
      String message,
      List<JourneyChatMessage> conversationHistory,
      JourneyTrailSnapshot currentJourney,
      Long trailId) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("model", aiProperties.getModel());
    payload.put("temperature", aiProperties.getTemperature());
    payload.put("max_output_tokens", Math.min(aiProperties.getMaxTokens() == null ? 700 : aiProperties.getMaxTokens(), 900));
    payload.put(
        "input",
        List.of(
            Map.of("role", "system", "content", buildTextContent(buildSystemPrompt())),
            Map.of("role", "user", "content", buildTextContent(buildUserPrompt(message, conversationHistory, currentJourney, trailId)))));
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
      Long trailId) {
    try {
      var payload = new LinkedHashMap<String, Object>();
      payload.put("mensagemUsuario", message);
      payload.put("trailIdSolicitado", trailId);
      payload.put("historicoCurto", normalizeHistory(conversationHistory));
      payload.put("jornadaAtual", currentJourney == null ? null : buildJourneyPayload(currentJourney));
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Nao foi possivel serializar o contexto da conversa.", exception);
    }
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
    payload.put("contentExcerpt", truncate(journey.content(), 4000));
    payload.put("mediaLinks", journey.mediaLinks());
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
      String message, JourneyTrailSnapshot currentJourney, String riskLevel, boolean fallbackUsed) {
    var journeyTitle = currentJourney == null || isBlank(currentJourney.title()) ? "sua jornada atual" : currentJourney.title();
    var reply =
        "Vou apoiar pelo caminho mais seguro agora: conecte sua pergunta com "
            + journeyTitle
            + " e escolha uma acao pequena antes de tentar resolver tudo. Pelo que voce trouxe, vale nomear o ponto central em uma frase e observar o que o corpo pede primeiro.\n\n"
            + "Se quiser continuar, me diga o que ficou mais dificil neste passo e eu ajudo a transformar isso em um movimento mais simples.";
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
}
