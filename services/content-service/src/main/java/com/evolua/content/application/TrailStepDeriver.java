package com.evolua.content.application;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailMediaLink;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class TrailStepDeriver {
  private static final Pattern HEADING_PATTERN = Pattern.compile("^#{1,3}\\s+(.+)$");
  private static final int MAX_STEPS = 8;

  public List<TrailJourneyStep> derive(Trail trail) {
    var steps = deriveFromMarkdown(trail);
    if (steps.size() >= 2) {
      return steps;
    }
    return fallbackSteps(trail);
  }

  private List<TrailJourneyStep> deriveFromMarkdown(Trail trail) {
    var content = trail.content() == null ? "" : trail.content();
    var lines = content.split("\\R");
    var sections = new ArrayList<Section>();
    Section current = null;

    for (var line : lines) {
      var matcher = HEADING_PATTERN.matcher(line.trim());
      if (matcher.matches()) {
        var title = matcher.group(1).trim();
        if (title.equalsIgnoreCase(trail.title())) {
          current = null;
          continue;
        }
        current = new Section(title);
        sections.add(current);
        if (sections.size() >= MAX_STEPS) {
          break;
        }
        continue;
      }

      if (current != null) {
        current.content.append(line).append("\n");
      }
    }

    var steps = new ArrayList<TrailJourneyStep>();
    for (var section : sections) {
      var sectionContent = section.content.toString().trim();
      steps.add(
          baseStep(
              steps.size(),
              section.title,
              firstSentence(sectionContent, trail.summary()),
              sectionContent.isBlank() ? trail.summary() : sectionContent,
              mediaLinksForStep(trail, steps.size(), sections.size())));
    }
    return steps;
  }

  private List<TrailJourneyStep> fallbackSteps(Trail trail) {
    var steps = new ArrayList<TrailJourneyStep>();
    steps.add(
        baseStep(
            0,
            "Entender seu ponto de partida",
            trail.summary(),
            trail.summary(),
            mediaLinksForStep(trail, 0, 3)));
    steps.add(
        baseStep(
            1,
            "Praticar o proximo passo",
            "Uma pratica curta para transformar a trilha em acao.",
            compactContent(trail.content(), trail.summary()),
            mediaLinksForStep(trail, 1, 3)));
    steps.add(
        baseStep(
            2,
            "Integrar no seu ritmo",
            "Feche a etapa com uma acao pequena e repetivel.",
            "Observe o que mudou depois da pratica e escolha um ajuste simples para repetir amanha.",
            mediaLinksForStep(trail, 2, 3)));
    return steps;
  }

  private TrailJourneyStep baseStep(
      int index, String title, String summary, String content, List<TrailMediaLink> mediaLinks) {
    return new TrailJourneyStep(
        index,
        title,
        summary == null || summary.isBlank() ? "Um passo simples da sua jornada." : summary,
        content == null || content.isBlank() ? summary : content,
        "upcoming",
        Math.min(12, Math.max(5, 5 + index * 2)),
        mediaLinks);
  }

  private List<TrailMediaLink> mediaLinksForStep(Trail trail, int index, int stepCount) {
    if (trail.mediaLinks() == null || trail.mediaLinks().isEmpty()) {
      return List.of();
    }
    if (index == Math.max(0, stepCount - 1)) {
      return trail.mediaLinks();
    }
    return List.of();
  }

  private String firstSentence(String content, String fallback) {
    var normalized = content == null ? "" : content.replaceAll("[\\r\\n#*_`-]+", " ").trim();
    if (normalized.isBlank()) {
      return fallback;
    }
    var dot = normalized.indexOf('.');
    if (dot > 20) {
      return normalized.substring(0, Math.min(dot + 1, 180));
    }
    return normalized.length() > 180 ? normalized.substring(0, 177) + "..." : normalized;
  }

  private String compactContent(String content, String fallback) {
    if (content == null || content.isBlank()) {
      return fallback;
    }
    var normalized = content.trim();
    return normalized.length() > 900 ? normalized.substring(0, 897) + "..." : normalized;
  }

  private static class Section {
    private final String title;
    private final StringBuilder content = new StringBuilder();

    private Section(String title) {
      this.title = title;
    }
  }
}
