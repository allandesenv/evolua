package com.evolua.chat.config;

import com.evolua.chat.infrastructure.persistence.MessageDocument;
import com.evolua.chat.infrastructure.persistence.MessageMongoRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class LocalSeedConfig {
  @Bean
  ApplicationRunner chatSeedRunner(MessageMongoRepository repository) {
    return args ->
        List.of(
                message(
                    "seed-message-1",
                    "clara-rocha",
                    "leo-respiro",
                    "Oi, Leo. Vou retomar a trilha de respiracao hoje. Depois me conta se voce ainda usa aquela rotina curta de pausa.",
                    "2026-03-20T08:10:00Z"),
                message(
                    "seed-message-2",
                    "clara-rocha",
                    "nina-fluxo",
                    "Nina, voce topa fazermos um check-in rapido no fim da tarde? Estou tentando sustentar melhor meu ritmo.",
                    "2026-03-20T16:30:00Z"),
                message(
                    "seed-message-3",
                    "clara-rocha",
                    "leo-respiro",
                    "Funcionou bem diminuir o volume das notificacoes antes de dormir. Parece pequeno, mas ajudou bastante.",
                    "2026-03-21T21:05:00Z"),
                message(
                    "seed-message-4",
                    "clara-rocha",
                    "nina-fluxo",
                    "Hoje eu consegui dizer nao para uma reuniao extra. Estou comemorando limites pequenos.",
                    "2026-03-22T14:12:00Z"),
                message(
                    "seed-message-5",
                    "clara-rocha",
                    "leo-respiro",
                    "Quero voltar a caminhar dez minutos depois do almoco. Se eu esquecer, me cobra amanha.",
                    "2026-03-23T11:48:00Z"),
                message(
                    "seed-message-6",
                    "clara-rocha",
                    "nina-fluxo",
                    "A trilha de foco gentil foi a que mais combinou com minha semana. Me senti menos pressionada.",
                    "2026-03-23T18:35:00Z"),
                message(
                    "seed-message-7",
                    "clara-rocha",
                    "leo-respiro",
                    "Estou montando uma noite mais tranquila: banho cedo, luz baixa e celular longe da cama.",
                    "2026-03-24T20:20:00Z"),
                message(
                    "seed-message-8",
                    "clara-rocha",
                    "nina-fluxo",
                    "Se der, vamos trocar tres pequenas vitorias da semana amanha cedo.",
                    "2026-03-25T07:40:00Z"),
                message(
                    "seed-message-9",
                    "clara-rocha",
                    "leo-respiro",
                    "A respiracao antes de reagir virou meu lembrete favorito para comecar o dia.",
                    "2026-03-25T09:55:00Z"),
                message(
                    "seed-message-10",
                    "clara-rocha",
                    "nina-fluxo",
                    "Obrigada por me lembrar de manter a meta pequena. Ficou muito mais facil continuar.",
                    "2026-03-25T17:10:00Z"))
            .forEach(
                item -> {
                  if (!repository.existsById(item.getId())) {
                    repository.save(item);
                  }
                });
  }

  private MessageDocument message(String id, String userId, String recipientId, String content, String createdAt) {
    MessageDocument document = new MessageDocument();
    document.setId(id);
    document.setUserId(userId);
    document.setRecipientId(recipientId);
    document.setContent(content);
    document.setCreatedAt(Instant.parse(createdAt));
    return document;
  }
}
