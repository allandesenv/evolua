package com.evolua.ai.application;

import com.evolua.ai.config.AiProperties;
import com.evolua.ai.infrastructure.security.AuthenticatedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class OpenAiWellBeingInsightGenerator implements WellBeingInsightGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiWellBeingInsightGenerator.class);

  private final AiProperties aiProperties;
  private final RuleBasedWellBeingInsightGenerator heuristicGenerator;
  private final SubscriptionQuotaClient subscriptionQuotaClient;
  private final CuratedJourneyLinkLibrary linkLibrary;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public OpenAiWellBeingInsightGenerator(
      AiProperties aiProperties,
      RuleBasedWellBeingInsightGenerator heuristicGenerator,
      SubscriptionQuotaClient subscriptionQuotaClient,
      CuratedJourneyLinkLibrary linkLibrary,
      ObjectMapper objectMapper) {
    this.aiProperties = aiProperties;
    this.heuristicGenerator = heuristicGenerator;
    this.subscriptionQuotaClient = subscriptionQuotaClient;
    this.linkLibrary = linkLibrary;
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

  @Override
  @SuppressWarnings("unchecked")
  public CheckInInsight generate(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      List<TrailCandidate> candidates,
      List<SpaceCandidate> spaces,
      AuthenticatedUser currentUser) {
    var baseline = heuristicGenerator.generate(currentCheckIn, context, candidates, spaces, currentUser);
    if ("high".equals(baseline.riskLevel())) {
      return baseline;
    }

    if (isBlank(aiProperties.getApiKey()) || isBlank(aiProperties.getModel())) {
      LOGGER.warn("Check-in insight OpenAI unavailable; using fallback. reason=missing_config");
      return markFallbackUsed(baseline);
    }

    var quota = subscriptionQuotaClient.consume(currentUser.userId());
    if (!Boolean.TRUE.equals(quota.allowed())) {
      return markFallbackUsed(baseline).withQuotaMetadata(quota, true);
    }

    try {
      var accessibleCandidates =
          candidates.stream()
              .filter(item -> Boolean.TRUE.equals(item.accessible()))
              .limit(8)
              .toList();
      var visibleSpaces =
          spaces.stream()
              .filter(item -> !"PRIVATE".equalsIgnoreCase(item.visibility()))
              .limit(8)
              .toList();

      Map<String, Object> payload =
          restClient
              .post()
              .uri("/responses")
              .body(buildRequest(currentCheckIn, context, accessibleCandidates, visibleSpaces, currentUser.roles(), baseline, quota))
              .retrieve()
              .body(Map.class);

      var structured = parseStructuredPayload(payload);
      if (structured.isEmpty()) {
        logFallback("empty_structured_payload", null);
        return fallbackOrBaseline(baseline);
      }

      var insight = stringValue(structured.get("insight"));
      var suggestedAction = stringValue(structured.get("suggestedAction"));
      var riskLevel = normalizeRisk(stringValue(structured.get("riskLevel")), baseline.riskLevel());
      var trailReason = stringValue(structured.get("suggestedTrailReason"));

      if (insight.isBlank() || suggestedAction.isBlank() || trailReason.isBlank()) {
        logFallback("invalid_structured_payload", null);
        return fallbackOrBaseline(baseline);
      }

      var generatedTrailDraft = parseGeneratedTrailDraft(structured.get("generatedTrailDraft"), baseline.generatedTrailDraft());
      var journeyPlan = parseJourneyPlan(structured.get("journeyPlan"), baseline.journeyPlan());
      var suggestedSpace = parseSuggestedSpace(structured.get("suggestedSpace"), visibleSpaces, baseline.suggestedSpace());

      return new CheckInInsight(
          insight,
          suggestedAction,
          riskLevel,
          null,
          generatedTrailDraft == null ? baseline.suggestedTrailTitle() : generatedTrailDraft.title(),
          trailReason,
          suggestedSpace,
          journeyPlan,
          generatedTrailDraft,
          false,
          false,
          quota.remainingToday(),
          quota.rewardedAdAvailable(),
          quota.upgradeRecommended(),
          quota.limitMessage());
    } catch (Exception exception) {
      logFallback("request_exception", exception);
      return fallbackOrBaseline(baseline).withQuotaMetadata(quota, false);
    }
  }

  private Map<String, Object> buildRequest(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      List<TrailCandidate> candidates,
      List<SpaceCandidate> spaces,
      List<String> roles,
      CheckInInsight baseline,
      AiQuotaDecision quota) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("model", aiProperties.getModel());
    payload.put("temperature", aiProperties.getTemperature());
    payload.put(
        "max_output_tokens",
        Boolean.TRUE.equals(quota.premium())
            ? aiProperties.getMaxTokens()
            : Math.min(aiProperties.getMaxTokens() == null ? 450 : aiProperties.getMaxTokens(), 450));
    payload.put(
        "input",
        List.of(
            Map.of("role", "system", "content", buildTextContent(buildSystemPrompt())),
            Map.of(
                "role",
                "user",
                "content",
                buildTextContent(buildUserPrompt(currentCheckIn, context, candidates, spaces, roles, baseline)))));
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
        "check_in_journey_insight",
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
                "insight",
                Map.of("type", "string"),
                "suggestedAction",
                Map.of("type", "string"),
                "riskLevel",
                Map.of("type", "string", "enum", List.of("low", "medium", "high")),
                "suggestedTrailReason",
                Map.of("type", "string"),
                "suggestedSpace",
                Map.of(
                    "type",
                    "object",
                    "additionalProperties",
                    false,
                    "properties",
                    Map.of(
                        "slug", Map.of("type", List.of("string", "null")),
                        "name", Map.of("type", List.of("string", "null")),
                        "reason", Map.of("type", List.of("string", "null"))),
                    "required",
                    List.of("slug", "name", "reason")),
                "journeyPlan",
                Map.of(
                    "type",
                    "object",
                    "additionalProperties",
                    false,
                    "properties",
                    Map.of(
                        "journeyKey", Map.of("type", "string"),
                        "journeyTitle", Map.of("type", "string"),
                        "phaseLabel", Map.of("type", "string"),
                        "continuityMode", Map.of("type", "string"),
                        "summary", Map.of("type", "string"),
                        "nextCheckInPrompt", Map.of("type", "string")),
                    "required",
                    List.of(
                        "journeyKey",
                        "journeyTitle",
                        "phaseLabel",
                        "continuityMode",
                        "summary",
                        "nextCheckInPrompt")),
                "generatedTrailDraft",
                Map.of(
                    "type",
                    "object",
                    "additionalProperties",
                    false,
                    "properties",
                    Map.of(
                        "title", Map.of("type", "string"),
                        "summary", Map.of("type", "string"),
                        "content", Map.of("type", "string"),
                        "category", Map.of("type", "string"),
                        "sourceStyle", Map.of("type", "string"),
                        "mediaLinks",
                        Map.of(
                            "type",
                            "array",
                            "items",
                            Map.of(
                                "type",
                                "object",
                                "additionalProperties",
                                false,
                                "properties",
                                Map.of(
                                    "label", Map.of("type", "string"),
                                    "url", Map.of("type", "string"),
                                    "type", Map.of("type", "string")),
                                "required",
                                List.of("label", "url", "type")))),
                    "required",
                    List.of("title", "summary", "content", "category", "sourceStyle", "mediaLinks"))),
            "required",
            List.of(
                "insight",
                "suggestedAction",
                "riskLevel",
                "suggestedTrailReason",
                "suggestedSpace",
                "journeyPlan",
                "generatedTrailDraft")));
  }

  private String buildSystemPrompt() {
    return """
        Voce e o motor de jornada da Evolua.
        Responda sempre em %s.
        Atue como apoio psicoeducativo e de autocuidado, nunca como diagnostico, prescricao clinica ou substituto de terapia.
        Use uma curadoria guiada inspirada em neurociencia aplicada, estoicismo, contemplacao biblica, imaginacao orientada e linguagem simbolica poetica, sem citar autores ou textos sagrados de forma literal por padrao.
        Sua tarefa e transformar o check-in do usuario em uma jornada privada, pratica, acolhedora e engajadora.
        O insight e a suggestedAction devem soar humanos, calorosos e fieis ao que o usuario realmente contou.
        Quando houver reflection, reconheca 1 ou 2 elementos concretos do relato e conecte isso ao historico recente sem copiar o texto inteiro.
        Evite respostas macro, stock phrases e aberturas repetidas em todos os casos.
        Varie a abertura, a justificativa e o proximo passo conforme humor, energia, reflection e tendencia recente.
        O campo generatedTrailDraft.content deve ser Markdown completo, com estas secoes obrigatorias: Leitura do momento, Direcao da jornada, Conversa guiada, Dicas praticas, Exercicios, Plano de 24 horas, Plano de 7 dias, Espaco sugerido, Proximos check-ins e Lembrete de seguranca.
        A jornada deve caber em 10 a 20 minutos de pratica inicial e ser profunda sem ser longa demais.
        Em mediaLinks, nao invente URLs. Se nao tiver certeza absoluta, retorne uma lista vazia; o sistema vai anexar links curados.
        Nao invente espacos fora da lista recebida.
        Nao invente IDs de trilhas.
        Se o contexto pedir cuidado, privilegie regulacao, seguranca e passos pequenos.
        """
        .formatted(aiProperties.getLanguage());
  }

  private String buildUserPrompt(
      CurrentCheckInInput currentCheckIn,
      EmotionalContextSnapshot context,
      List<TrailCandidate> candidates,
      List<SpaceCandidate> spaces,
      List<String> roles,
      CheckInInsight baseline) {
    try {
      var contextPayload = new LinkedHashMap<String, Object>();
      contextPayload.put("checkInAtual", buildCurrentCheckInPayload(currentCheckIn));
      contextPayload.put("historicoRecente", buildHistoryPayload(context));
      contextPayload.put(
          "trilhasCatalogo",
          candidates.stream().map(this::buildTrailCandidatePayload).collect(Collectors.toCollection(ArrayList::new)));
      contextPayload.put(
          "espacosDisponiveis",
          spaces.stream().map(this::buildSpacePayload).collect(Collectors.toCollection(ArrayList::new)));
      contextPayload.put("rolesUsuario", roles);
      contextPayload.put("baselineSeguro", buildBaselinePayload(baseline));
      contextPayload.put(
          "diretivasDeEstilo",
          Map.of(
              "voice", "acolhedora e fiel",
              "citeConcreteDetailsWhenAvailable", true,
              "avoidStockPhrases", true,
              "contextConfidence",
              currentCheckIn.reflection() == null || currentCheckIn.reflection().isBlank() ? "medium" : "high"));
      return objectMapper.writeValueAsString(contextPayload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Nao foi possivel serializar o contexto da IA.", exception);
    }
  }

  private Map<String, Object> buildCurrentCheckInPayload(CurrentCheckInInput currentCheckIn) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("mood", safeString(currentCheckIn.mood()));
    payload.put("reflection", safeString(currentCheckIn.reflection()));
    payload.put("energyLevel", currentCheckIn.energyLevel());
    return payload;
  }

  private Map<String, Object> buildHistoryPayload(EmotionalContextSnapshot context) {
    var recentCheckIns =
        context.recentCheckIns().stream()
            .limit(5)
            .map(
                item -> {
                  var snapshot = new LinkedHashMap<String, Object>();
                  snapshot.put("mood", safeString(item.mood()));
                  snapshot.put("reflection", safeString(item.reflection()));
                  snapshot.put("energyLevel", item.energyLevel());
                  snapshot.put("createdAt", item.createdAt() == null ? null : item.createdAt().toString());
                  return snapshot;
                })
            .toList();

    var historyPayload = new LinkedHashMap<String, Object>();
    historyPayload.put("averageEnergy", context.averageEnergy());
    historyPayload.put("dominantMood", safeString(context.dominantMood()));
    historyPayload.put("energyTrendLabel", safeString(context.energyTrendLabel()));
    historyPayload.put("recentCheckIns", recentCheckIns);
    return historyPayload;
  }

  private Map<String, Object> buildTrailCandidatePayload(TrailCandidate item) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("id", item.id());
    payload.put("title", safeString(item.title()));
    payload.put("summary", safeString(item.summary()));
    payload.put("category", safeString(item.category()));
    payload.put("premium", Boolean.TRUE.equals(item.premium()));
    return payload;
  }

  private Map<String, Object> buildSpacePayload(SpaceCandidate item) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("id", safeString(item.id()));
    payload.put("slug", safeString(item.slug()));
    payload.put("name", safeString(item.name()));
    payload.put("description", safeString(item.description()));
    payload.put("category", safeString(item.category()));
    payload.put("joined", Boolean.TRUE.equals(item.joined()));
    return payload;
  }

  private Map<String, Object> buildBaselinePayload(CheckInInsight baseline) {
    var payload = new LinkedHashMap<String, Object>();
    payload.put("riskLevel", baseline.riskLevel());
    payload.put("suggestedTrailTitle", safeString(baseline.suggestedTrailTitle()));
    payload.put("suggestedTrailReason", safeString(baseline.suggestedTrailReason()));
    payload.put("journeyPlan", baseline.journeyPlan() == null ? null : baseline.journeyPlan().journeyKey());
    payload.put("sourceStyle", baseline.generatedTrailDraft() == null ? null : baseline.generatedTrailDraft().sourceStyle());
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
        if (!(contentItem instanceof Map<?, ?> contentMap)) {
          continue;
        }
        var text = stringValue(contentMap.get("text"));
        if (!text.isBlank()) {
          return objectMapper.readValue(text, Map.class);
        }
      }
    }
    return Map.of();
  }

  @SuppressWarnings("unchecked")
  private GeneratedTrailDraft parseGeneratedTrailDraft(Object raw, GeneratedTrailDraft fallback) {
    if (!(raw instanceof Map<?, ?> map)) {
      return fallback;
    }
    var mediaLinksRaw = map.get("mediaLinks") instanceof List<?> list ? list : List.of();
    var mediaLinks =
        mediaLinksRaw.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(
                item ->
                    new GeneratedTrailMediaLink(
                        stringValue(item.get("label")),
                        stringValue(item.get("url")),
                        stringValue(item.get("type"))))
            .toList();

    var title = stringValue(map.get("title"));
    var summary = stringValue(map.get("summary"));
    var content = stringValue(map.get("content"));
    var category = stringValue(map.get("category"));
    var sourceStyle = stringValue(map.get("sourceStyle"));
    if (title.isBlank() || summary.isBlank() || content.isBlank() || category.isBlank()) {
      return fallback;
    }
    return new GeneratedTrailDraft(
        title, summary, content, category, sourceStyle, linkLibrary.sanitizeOrCurate(category, mediaLinks));
  }

  @SuppressWarnings("unchecked")
  private JourneyPlan parseJourneyPlan(Object raw, JourneyPlan fallback) {
    if (!(raw instanceof Map<?, ?> map)) {
      return fallback;
    }
    var journeyKey = stringValue(map.get("journeyKey"));
    var journeyTitle = stringValue(map.get("journeyTitle"));
    var phaseLabel = stringValue(map.get("phaseLabel"));
    var continuityMode = stringValue(map.get("continuityMode"));
    var summary = stringValue(map.get("summary"));
    var nextCheckInPrompt = stringValue(map.get("nextCheckInPrompt"));
    if (journeyKey.isBlank() || journeyTitle.isBlank() || continuityMode.isBlank()) {
      return fallback;
    }
    return new JourneyPlan(
        journeyKey, journeyTitle, phaseLabel, continuityMode, summary, nextCheckInPrompt);
  }

  @SuppressWarnings("unchecked")
  private SuggestedSpace parseSuggestedSpace(
      Object raw, List<SpaceCandidate> spaces, SuggestedSpace fallback) {
    if (!(raw instanceof Map<?, ?> map)) {
      return fallback;
    }
    var slug = stringValue(map.get("slug"));
    var reason = stringValue(map.get("reason"));
    var chosen =
        spaces.stream().filter(item -> Objects.equals(item.slug(), slug)).findFirst().orElse(null);
    if (chosen == null) {
      return fallback;
    }
    return new SuggestedSpace(chosen.id(), chosen.slug(), chosen.name(), reason);
  }

  private CheckInInsight fallbackOrBaseline(CheckInInsight baseline) {
    return Boolean.TRUE.equals(aiProperties.getFallbackEnabled()) ? markFallbackUsed(baseline) : baseline;
  }

  private void logFallback(String reason, Exception exception) {
    if (exception instanceof RestClientResponseException responseException) {
      LOGGER.warn(
          "Check-in insight OpenAI request failed; using fallback. reason={} status={} response={}",
          reason,
          responseException.getStatusCode().value(),
          safeResponseCode(responseException.getResponseBodyAsString()));
      return;
    }

    if (exception == null) {
      LOGGER.warn("Check-in insight OpenAI response invalid; using fallback. reason={}", reason);
      return;
    }

    LOGGER.warn(
        "Check-in insight OpenAI request failed; using fallback. reason={} exception={}",
        reason,
        exception.getClass().getSimpleName());
  }

  private String safeResponseCode(String responseBody) {
    var body = safeString(responseBody);
    var normalized = body.toLowerCase(Locale.ROOT);
    for (var code : List.of("insufficient_quota", "rate_limit_exceeded", "invalid_api_key", "model_not_found")) {
      if (normalized.contains(code)) {
        return code;
      }
    }
    return "unavailable";
  }

  private CheckInInsight markFallbackUsed(CheckInInsight baseline) {
    return new CheckInInsight(
        baseline.insight(),
        baseline.suggestedAction(),
        baseline.riskLevel(),
        baseline.suggestedTrailId(),
        baseline.suggestedTrailTitle(),
        baseline.suggestedTrailReason(),
        baseline.suggestedSpace(),
        baseline.journeyPlan(),
        baseline.generatedTrailDraft(),
        true);
  }

  private String safeString(String value) {
    return value == null ? "" : value;
  }

  private String stringValue(Object value) {
    return value == null ? "" : value.toString().trim();
  }

  private String normalizeRisk(String riskLevel, String fallbackValue) {
    var normalized = riskLevel == null ? "" : riskLevel.toLowerCase(Locale.ROOT).trim();
    return switch (normalized) {
      case "low", "medium", "high" -> normalized;
      default -> fallbackValue;
    };
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
