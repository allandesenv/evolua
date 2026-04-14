package com.evolua.notification.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CheckInReminderScheduler {
  private final NotificationJobService notificationJobService;
  private final boolean enabled;
  private final List<String> targetUserIds;

  public CheckInReminderScheduler(
      NotificationJobService notificationJobService,
      @Value("${app.notifications.checkin-reminder.enabled:true}") boolean enabled,
      @Value("${app.notifications.checkin-reminder.user-ids:clara-rocha,leo-respiro}")
          String targetUserIds) {
    this.notificationJobService = notificationJobService;
    this.enabled = enabled;
    this.targetUserIds =
        Arrays.stream(targetUserIds.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
  }

  @Scheduled(cron = "${app.notifications.checkin-reminder.cron:0 0 9 * * *}")
  public void createDailyReminders() {
    if (!enabled) {
      return;
    }

    var since = Instant.now().minus(20, ChronoUnit.HOURS);
    for (var userId : targetUserIds) {
      if (!notificationJobService.hasRecentReminder(userId, "CHECKIN_REMINDER", since)) {
        notificationJobService.createSystem(
            userId,
            "CHECKIN_REMINDER",
            "Hora do seu check-in",
            "Passe no app por alguns minutos e registre como voce esta chegando hoje.",
            "/home");
      }
    }
  }
}
