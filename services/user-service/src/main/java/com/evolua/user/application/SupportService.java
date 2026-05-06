package com.evolua.user.application;

import com.evolua.user.domain.SupportTicket;
import com.evolua.user.domain.SupportTicketRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportService {
  private static final Set<String> CATEGORIES = Set.of("GENERAL", "SUPPORT", "TECHNICAL", "BILLING");
  private static final String OPEN_STATUS = "OPEN";

  private final SupportTicketRepository repository;
  private final SupportStatusHealthClient healthClient;
  private final SupportConfig config;
  private final SupportHealthUrls healthUrls;

  public SupportService(
      SupportTicketRepository repository,
      SupportStatusHealthClient healthClient,
      @Value("${app.support.links.help-center-url:}") String helpCenterUrl,
      @Value("${app.support.links.support-url:}") String supportUrl,
      @Value("${app.support.links.professional-help-url:}") String professionalHelpUrl,
      @Value("${app.support.links.emotional-resources-url:}") String emotionalResourcesUrl,
      @Value("${app.support.links.ai-limits-url:}") String aiLimitsUrl,
      @Value("${app.support.status.system-health-url:http://localhost:8082/actuator/health}") String systemHealthUrl,
      @Value("${app.support.status.ai-health-url:http://localhost:8089/actuator/health}") String aiHealthUrl,
      @Value("${app.support.status.notifications-health-url:http://localhost:8088/actuator/health}") String notificationsHealthUrl,
      @Value("${app.support.status.sync-health-url:http://localhost:8083/actuator/health}") String syncHealthUrl) {
    this.repository = repository;
    this.healthClient = healthClient;
    this.config =
        new SupportConfig(
            blankToNull(helpCenterUrl),
            blankToNull(supportUrl),
            blankToNull(professionalHelpUrl),
            blankToNull(emotionalResourcesUrl),
            blankToNull(aiLimitsUrl));
    this.healthUrls =
        new SupportHealthUrls(
            blankToNull(systemHealthUrl),
            blankToNull(aiHealthUrl),
            blankToNull(notificationsHealthUrl),
            blankToNull(syncHealthUrl));
  }

  @Transactional
  public SupportTicket createTicket(
      String userId, String email, String category, String subject, String message) {
    var normalizedCategory = normalizeCategory(category);
    var normalizedSubject = required(subject, "Informe o assunto do chamado.");
    var normalizedMessage = required(message, "Conte rapidamente como podemos ajudar.");
    if (normalizedSubject.length() > 160) {
      throw new IllegalArgumentException("Use um assunto com ate 160 caracteres.");
    }
    if (normalizedMessage.length() > 4000) {
      throw new IllegalArgumentException("Use uma mensagem com ate 4000 caracteres.");
    }

    var now = Instant.now();
    return repository.save(
        new SupportTicket(
            null,
            userId,
            email,
            normalizedCategory,
            normalizedSubject,
            normalizedMessage,
            OPEN_STATUS,
            now,
            now));
  }

  public SupportConfig config() {
    return config;
  }

  public List<SupportStatusItem> status() {
    return List.of(
        statusItem("system", "Sistema operacional", healthUrls.systemHealthUrl()),
        statusItem("ai", "IA operacional", healthUrls.aiHealthUrl()),
        statusItem("notifications", "Notificacoes", healthUrls.notificationsHealthUrl()),
        statusItem("sync", "Sincronizacao", healthUrls.syncHealthUrl()));
  }

  private SupportStatusItem statusItem(String key, String label, String url) {
    if (url == null) {
      return new SupportStatusItem(key, label, "UNKNOWN", "Verificacao nao configurada.");
    }
    var healthy = healthClient.isHealthy(url);
    return new SupportStatusItem(
        key,
        label,
        healthy ? "OPERATIONAL" : "UNKNOWN",
        healthy ? "Funcionando normalmente." : "Nao foi possivel confirmar agora.");
  }

  private String normalizeCategory(String category) {
    var normalized = category == null || category.isBlank() ? "GENERAL" : category.trim().toUpperCase();
    if (!CATEGORIES.contains(normalized)) {
      throw new IllegalArgumentException("Categoria de suporte invalida.");
    }
    return normalized;
  }

  private String required(String value, String message) {
    if (value == null || value.trim().isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  public record SupportConfig(
      String helpCenterUrl,
      String supportUrl,
      String professionalHelpUrl,
      String emotionalResourcesUrl,
      String aiLimitsUrl) {}

  public record SupportStatusItem(String key, String label, String state, String detail) {}

  private record SupportHealthUrls(
      String systemHealthUrl,
      String aiHealthUrl,
      String notificationsHealthUrl,
      String syncHealthUrl) {}
}
