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
  private static final String USER_ID = "clara-rocha";
  private static final String LIST_KEY = "notification-jobs:" + USER_ID;
  private static final String MARKER_KEY = "notification-jobs:seed:v1:" + USER_ID;

  @Bean
  ApplicationRunner notificationSeedRunner(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    return args -> {
      if (Boolean.TRUE.equals(redisTemplate.hasKey(MARKER_KEY))) {
        return;
      }

      List<NotificationJob> items =
          List.of(
              notification("seed-notification-1", "IN_APP", "Sua trilha de foco gentil esta pronta para retomar.", "2026-03-24T08:00:00Z"),
              notification("seed-notification-2", "PUSH", "Hora de fazer um check-in rapido e notar como voce chegou ate aqui.", "2026-03-24T18:00:00Z"),
              notification("seed-notification-3", "EMAIL", "Resumo da semana: voce manteve pequenos passos com mais constancia.", "2026-03-25T07:15:00Z"),
              notification("seed-notification-4", "IN_APP", "A comunidade geral recebeu novas conversas sobre recomeco e rotina.", "2026-03-25T12:20:00Z"));

      for (NotificationJob item : items) {
        redisTemplate.opsForList().rightPush(LIST_KEY, objectMapper.writeValueAsString(item));
      }

      redisTemplate.opsForValue().set(MARKER_KEY, Instant.now().toString());
    };
  }

  private NotificationJob notification(String id, String channel, String message, String createdAt) {
    return new NotificationJob(id, USER_ID, channel, message, Instant.parse(createdAt));
  }
}
