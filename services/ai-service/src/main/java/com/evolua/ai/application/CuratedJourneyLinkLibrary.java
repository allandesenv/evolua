package com.evolua.ai.application;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class CuratedJourneyLinkLibrary {
  public List<GeneratedTrailMediaLink> linksFor(String categoryOrJourneyKey) {
    var key = normalize(categoryOrJourneyKey);
    if (containsAny(key, List.of("ansiedade", "regulacao", "calma"))) {
      return List.of(
          link("Respiracao para momentos de estresse", "https://www.nhs.uk/mental-health/self-help/guides-tools-and-activities/breathing-exercises-for-stress/", "article"),
          link("Mindfulness e regulacao atencional", "https://www.apa.org/topics/mindfulness/meditation", "article"));
    }
    if (containsAny(key, List.of("energia", "recuperacao", "descanso"))) {
      return List.of(
          link("Sono e saude mental", "https://www.sleepfoundation.org/mental-health", "article"),
          link("Pausa consciente de 5 minutos", "https://www.youtube.com/watch?v=inpok4MKVLM", "youtube"));
    }
    if (containsAny(key, List.of("clareza", "foco", "constancia"))) {
      return List.of(
          link("Mindfulness e foco", "https://www.apa.org/topics/mindfulness/meditation", "article"),
          link("Habitos e saude comportamental", "https://www.nih.gov/news-events/nih-research-matters/how-habits-form", "article"));
    }
    return List.of(
        link("Respiracao para reduzir estresse", "https://www.nhs.uk/mental-health/self-help/guides-tools-and-activities/breathing-exercises-for-stress/", "article"),
        link("Mindfulness como pratica de autocuidado", "https://www.apa.org/topics/mindfulness/meditation", "article"));
  }

  public List<GeneratedTrailMediaLink> sanitizeOrCurate(
      String categoryOrJourneyKey, List<GeneratedTrailMediaLink> proposedLinks) {
    var safeProposed =
        proposedLinks == null
            ? List.<GeneratedTrailMediaLink>of()
            : proposedLinks.stream().filter(this::isAllowed).limit(3).toList();
    return safeProposed.isEmpty() ? linksFor(categoryOrJourneyKey) : safeProposed;
  }

  private GeneratedTrailMediaLink link(String label, String url, String type) {
    return new GeneratedTrailMediaLink(label, url, type);
  }

  private boolean isAllowed(GeneratedTrailMediaLink link) {
    if (link == null || link.url() == null || link.url().isBlank()) {
      return false;
    }
    try {
      var uri = URI.create(link.url().trim());
      var scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
      var host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
      return List.of("https").contains(scheme)
          && List.of(
                  "www.nhs.uk",
                  "nhs.uk",
                  "www.apa.org",
                  "apa.org",
                  "www.nih.gov",
                  "nih.gov",
                  "www.sleepfoundation.org",
                  "sleepfoundation.org",
                  "www.youtube.com",
                  "youtube.com",
                  "youtu.be")
              .contains(host);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  private boolean containsAny(String source, List<String> tokens) {
    return tokens.stream().anyMatch(source::contains);
  }

  private String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }
}
