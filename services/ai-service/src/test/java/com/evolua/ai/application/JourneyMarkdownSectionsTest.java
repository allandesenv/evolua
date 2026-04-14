package com.evolua.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JourneyMarkdownSectionsTest {
  @Test
  void extractsKeyJourneySectionsFromMarkdown() {
    var markdown =
        """
        # Jornada

        ## Leitura do momento
        Seu corpo esta pedindo pausa.

        ## Direcao da jornada
        Primeiro reduzir carga.

        ## Exercicios
        1. Respirar

        ## Plano de 24 horas
        Dormir mais cedo.

        ## Plano de 7 dias
        Repetir a pratica.
        """;

    var result = JourneyMarkdownSections.extract(markdown);

    assertEquals("Seu corpo esta pedindo pausa.", result.get("Leitura do momento"));
    assertEquals("Primeiro reduzir carga.", result.get("Direcao da jornada"));
    assertTrue(result.get("Exercicios").contains("Respirar"));
    assertTrue(result.get("Plano de 24 horas").contains("Dormir mais cedo"));
    assertTrue(result.get("Plano de 7 dias").contains("Repetir a pratica"));
  }
}
