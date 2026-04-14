package com.evolua.notification.config;

import com.evolua.notification.domain.NotificationJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class LocalSeedConfig {
  private static final String ADMIN_USER_ID = "clara-rocha";
  private static final String USER_ID = "leo-respiro";
  private static final String ADMIN_LIST_KEY = "notification-jobs:" + ADMIN_USER_ID;
  private static final String USER_LIST_KEY = "notification-jobs:" + USER_ID;
  private static final String MARKER_KEY = "notification-jobs:seed:v2";

  @Bean
  ApplicationRunner notificationSeedRunner(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    return args -> {
      if (Boolean.TRUE.equals(redisTemplate.hasKey(MARKER_KEY))) {
        return;
      }

      List<NotificationJob> items =
          List.of(
              notification(
                  "seed-notification-1",
                  USER_ID,
                  "CHECKIN_REMINDER",
                  "Seu check-in de hoje esta te esperando",
                  "Vale fazer um registro rapido para a jornada continuar no ritmo certo.",
                  "/home",
                  "SYSTEM",
                  null,
                  "2026-03-24T18:00:00Z",
                  null),
              notification(
                  "seed-notification-2",
                  USER_ID,
                  "EVENT",
                  "Sua jornada privada recebeu uma nova direcao",
                  "Abrimos um proximo passo mais leve para voce continuar sem se atropelar.",
                  "/home",
                  "SYSTEM",
                  null,
                  "2026-03-25T07:15:00Z",
                  null),
              notification(
                  "seed-notification-3",
                  ADMIN_USER_ID,
                  "ADMIN_MESSAGE",
                  "Painel de notificacoes pronto",
                  "Voce ja pode enviar comunicacoes manuais para usuarios quando necessario.",
                  "/profile",
                  "SYSTEM",
                  null,
                  "2026-03-25T12:20:00Z",
                  null));

      pushAll(redisTemplate, objectMapper, USER_LIST_KEY, items.stream().filter(item -> USER_ID.equals(item.userId())).toList());
      pushAll(redisTemplate, objectMapper, ADMIN_LIST_KEY, items.stream().filter(item -> ADMIN_USER_ID.equals(item.userId())).toList());

      redisTemplate.opsForValue().set(MARKER_KEY, Instant.now().toString());
    };
  }

  private void pushAll(
      StringRedisTemplate redisTemplate, ObjectMapper objectMapper, String key, List<NotificationJob> items)
      throws Exception {
    for (NotificationJob item : items) {
      redisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(item));
    }
  }

  private NotificationJob notification(
      String id,
      String userId,
      String type,
      String title,
      String message,
      String actionTarget,
      String source,
      String createdBy,
      String createdAt,
      String readAt) {
    return new NotificationJob(
        id,
        userId,
        type,
        title,
        message,
        actionTarget,
        source,
        createdBy,
        Instant.parse(createdAt),
        readAt == null ? null : Instant.parse(readAt));
  }
}
