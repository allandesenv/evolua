package com.evolua.content.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.evolua.content.domain.Trail;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrailStepDeriverTest {
  private final TrailStepDeriver deriver = new TrailStepDeriver();

  @Test
  void shouldCreateStepsFromMarkdownHeadings() {
    var trail =
        trail(
            """
            # Jornada de foco

            ## Respirar
            Volte para o corpo por dois minutos.

            ## Escolher
            Defina uma acao pequena.

            ## Integrar
            Anote o que mudou.
            """);

    var steps = deriver.derive(trail);

    assertThat(steps).hasSize(3);
    assertThat(steps.get(0).title()).isEqualTo("Respirar");
    assertThat(steps.get(1).title()).isEqualTo("Escolher");
    assertThat(steps.get(2).title()).isEqualTo("Integrar");
  }

  @Test
  void shouldCreateFallbackStepsWhenMarkdownHasNoHeadings() {
    var trail = trail("Conteudo simples sem secoes.");

    var steps = deriver.derive(trail);

    assertThat(steps).hasSize(3);
    assertThat(steps.get(0).title()).isEqualTo("Entender seu ponto de partida");
    assertThat(steps.get(1).title()).isEqualTo("Praticar o proximo passo");
    assertThat(steps.get(2).title()).isEqualTo("Integrar no seu ritmo");
  }

  private Trail trail(String content) {
    return new Trail(
        1L,
        "user-1",
        "Jornada de foco",
        "Resumo da trilha",
        content,
        "foco",
        false,
        true,
        true,
        true,
        "focus",
        "neuro",
        List.of(),
        Instant.now());
  }
}
