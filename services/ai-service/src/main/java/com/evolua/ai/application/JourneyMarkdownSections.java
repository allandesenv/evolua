package com.evolua.ai.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class JourneyMarkdownSections {
  private static final List<String> KEY_SECTIONS =
      List.of(
          "Leitura do momento",
          "Direcao da jornada",
          "Exercicios",
          "Plano de 24 horas",
          "Plano de 7 dias");

  private JourneyMarkdownSections() {}

  static Map<String, String> extract(String markdown) {
    if (markdown == null || markdown.isBlank()) {
      return Map.of();
    }

    var sections = new LinkedHashMap<String, String>();
    String currentSection = null;
    var buffer = new StringBuilder();

    for (var rawLine : markdown.split("\\R")) {
      var line = rawLine == null ? "" : rawLine.trim();
      if (line.startsWith("## ")) {
        flushSection(sections, currentSection, buffer);
        var heading = line.substring(3).trim();
        currentSection = KEY_SECTIONS.contains(heading) ? heading : null;
        buffer.setLength(0);
        continue;
      }
      if (currentSection != null) {
        if (!buffer.isEmpty()) {
          buffer.append('\n');
        }
        buffer.append(rawLine);
      }
    }

    flushSection(sections, currentSection, buffer);
    return sections;
  }

  private static void flushSection(
      Map<String, String> sections, String currentSection, StringBuilder buffer) {
    if (currentSection == null) {
      return;
    }
    var value = buffer.toString().trim();
    if (!value.isBlank()) {
      sections.put(currentSection, value);
    }
  }
}
