package com.evolua.social.config;

import com.evolua.social.infrastructure.persistence.CommunityDocument;
import com.evolua.social.infrastructure.persistence.CommunityMongoRepository;
import com.evolua.social.infrastructure.persistence.PostDocument;
import com.evolua.social.infrastructure.persistence.PostMongoRepository;
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
  ApplicationRunner socialSeedRunner(CommunityMongoRepository communityRepository, PostMongoRepository repository) {
    return args -> {
      List.of(
              community(
                  "seed-community-1",
                  "geral",
                  "Geral",
                  "Um espaco amplo para compartilhar pequenas vitorias, recomecos e aprendizados do dia.",
                  "PUBLIC",
                  "acolhimento",
                  List.of("clara-rocha", "leo-respiro", "nina-fluxo"),
                  "2026-03-18T08:00:00Z"),
              community(
                  "seed-community-2",
                  "ansiedade",
                  "Ansiedade com mais apoio",
                  "Conversas praticas para desacelerar, nomear gatilhos e recuperar respiracao no meio do dia.",
                  "PRIVATE",
                  "emocional",
                  List.of("clara-rocha", "leo-respiro"),
                  "2026-03-18T08:10:00Z"),
              community(
                  "seed-community-3",
                  "sono",
                  "Sono que acolhe",
                  "Trocas sobre rotina noturna, higiene do sono e pequenos ajustes para descansar melhor.",
                  "PUBLIC",
                  "bem-estar",
                  List.of("clara-rocha", "leo-respiro"),
                  "2026-03-18T08:20:00Z"),
              community(
                  "seed-community-4",
                  "rotina",
                  "Rotina sem rigidez",
                  "Ideias simples para transformar intencao em consistencia sem entrar em autocobranca.",
                  "PUBLIC",
                  "habitos",
                  List.of("clara-rocha", "nina-fluxo"),
                  "2026-03-18T08:30:00Z"),
              community(
                  "seed-community-5",
                  "mindfulness",
                  "Mindfulness aplicavel",
                  "Praticas de presenca para quem vive com a agenda cheia e precisa de recursos curtos.",
                  "PUBLIC",
                  "presenca",
                  List.of("clara-rocha", "nina-fluxo"),
                  "2026-03-18T08:40:00Z"),
              community(
                  "seed-community-6",
                  "autoconhecimento",
                  "Autoconhecimento em movimento",
                  "Reflexoes sobre limites, comparacao, identidade e progresso com mais gentileza.",
                  "PRIVATE",
                  "reflexao",
                  List.of("clara-rocha"),
                  "2026-03-18T08:50:00Z"),
              community(
                  "seed-community-7",
                  "foco-compassivo",
                  "Foco compassivo",
                  "Descoberta para quem quer concentracao com menos rigidez e mais clareza.",
                  "PUBLIC",
                  "foco",
                  List.of("leo-respiro"),
                  "2026-03-18T09:00:00Z"))
          .forEach(
              item -> {
                if (!communityRepository.existsById(item.getId())) {
                  communityRepository.save(item);
                }
              });

      List.of(
              post(
                  "seed-post-1",
                  "clara-rocha",
                  "Hoje consegui trocar cinco minutos de pressa por cinco minutos de presenca. Foi pequeno, mas mudou o tom do dia.",
                  "geral",
                  "PUBLIC",
                  "2026-03-20T11:00:00Z"),
              post(
                  "seed-post-2",
                  "clara-rocha",
                  "Alguem tem uma rotina leve para desacelerar antes de dormir? Estou testando audio + luz mais baixa.",
                  "sono",
                  "PUBLIC",
                  "2026-03-20T20:30:00Z"),
              post(
                  "seed-post-3",
                  "clara-rocha",
                  "Voltei para a trilha de foco e percebi que pausas curtas estao me ajudando mais do que insistir cansada.",
                  "rotina",
                  "PUBLIC",
                  "2026-03-21T09:15:00Z"),
              post(
                  "seed-post-4",
                  "clara-rocha",
                  "Estou tentando normalizar dias medianos. Nem todo progresso precisa parecer grande para ser real.",
                  "autoconhecimento",
                  "PUBLIC",
                  "2026-03-21T18:40:00Z"),
              post(
                  "seed-post-5",
                  "clara-rocha",
                  "Hoje preferi ficar mais quieta e registrar primeiro o que senti antes de responder todo mundo.",
                  "ansiedade",
                  "PRIVATE",
                  "2026-03-22T08:05:00Z"),
              post(
                  "seed-post-6",
                  "clara-rocha",
                  "Minha mini vitoria da semana foi proteger a noite de domingo para recomecar sem correria na segunda.",
                  "rotina",
                  "PUBLIC",
                  "2026-03-22T19:20:00Z"),
              post(
                  "seed-post-7",
                  "clara-rocha",
                  "Usei uma pratica curta de mindfulness entre duas reunioes e a diferenca no meu corpo foi imediata.",
                  "mindfulness",
                  "PUBLIC",
                  "2026-03-23T14:10:00Z"),
              post(
                  "seed-post-8",
                  "clara-rocha",
                  "Quando eu fico insegura, tento me lembrar do que ja consegui sustentar por mais simples que pareca.",
                  "autoconhecimento",
                  "PRIVATE",
                  "2026-03-24T07:55:00Z"),
              post(
                  "seed-post-9",
                  "clara-rocha",
                  "Se voce estivesse recomecando a semana hoje, qual seria a menor acao possivel para se cuidar melhor?",
                  "geral",
                  "PUBLIC",
                  "2026-03-24T12:45:00Z"),
              post(
                  "seed-post-10",
                  "clara-rocha",
                  "Criei um lembrete de respiracao antes do almoco e isso diminuiu muito minha sensacao de atropelo.",
                  "ansiedade",
                  "PUBLIC",
                  "2026-03-25T10:30:00Z"))
          .forEach(
              item -> {
                if (!repository.existsById(item.getId())) {
                  repository.save(item);
                }
              });
    };
  }

  private CommunityDocument community(
      String id,
      String slug,
      String name,
      String description,
      String visibility,
      String category,
      List<String> memberIds,
      String createdAt) {
    CommunityDocument document = new CommunityDocument();
    document.setId(id);
    document.setSlug(slug);
    document.setName(name);
    document.setDescription(description);
    document.setVisibility(visibility);
    document.setCategory(category);
    document.setMemberIds(memberIds);
    document.setCreatedAt(Instant.parse(createdAt));
    return document;
  }

  private PostDocument post(
      String id,
      String userId,
      String content,
      String community,
      String visibility,
      String createdAt) {
    PostDocument document = new PostDocument();
    document.setId(id);
    document.setUserId(userId);
    document.setContent(content);
    document.setCommunity(community);
    document.setVisibility(visibility);
    document.setCreatedAt(Instant.parse(createdAt));
    return document;
  }
}
