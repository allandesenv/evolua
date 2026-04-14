package com.evolua.notification.infrastructure.persistence;

import com.evolua.notification.domain.NotificationJob;
import com.evolua.notification.domain.NotificationJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationJobRedisRepository implements NotificationJobRepository {
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public NotificationJobRedisRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public NotificationJob save(NotificationJob item) {
    var items = load(item.userId());
    items.add(item);
    saveAll(item.userId(), items);
    return item;
  }

  @Override
  public Page<NotificationJob> findAllByUserId(
      String userId, Pageable pageable, String search, String type, Boolean unreadOnly) {
    var filtered =
        load(userId).stream()
            .filter(
                item ->
                    search == null
                        || search.isBlank()
                        || contains(item.title(), search)
                        || contains(item.message(), search)
                        || contains(item.type(), search))
            .filter(item -> type == null || type.isBlank() || item.type().equalsIgnoreCase(type))
            .filter(item -> unreadOnly == null || !unreadOnly || item.readAt() == null)
            .sorted(comparator(pageable))
            .toList();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), filtered.size());
    List<NotificationJob> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);
    return new PageImpl<>(pageContent, pageable, filtered.size());
  }

  @Override
  public long countUnreadByUserId(String userId) {
    return load(userId).stream().filter(item -> item.readAt() == null).count();
  }

  @Override
  public NotificationJob markAsRead(String userId, String notificationId, Instant readAt) {
    var items = new ArrayList<>(load(userId));
    NotificationJob updated = null;
    for (int index = 0; index < items.size(); index++) {
      var item = items.get(index);
      if (item.id().equals(notificationId)) {
        updated =
            item.readAt() == null
                ? new NotificationJob(
                    item.id(),
                    item.userId(),
                    item.type(),
                    item.title(),
                    item.message(),
                    item.actionTarget(),
                    item.source(),
                    item.createdBy(),
                    item.createdAt(),
                    readAt)
                : item;
        items.set(index, updated);
        saveAll(userId, items);
        return updated;
      }
    }
    throw new IllegalArgumentException("Notification not found");
  }

  @Override
  public long markAllAsRead(String userId, Instant readAt) {
    var items = new ArrayList<>(load(userId));
    var updatedCount = 0L;
    for (int index = 0; index < items.size(); index++) {
      var item = items.get(index);
      if (item.readAt() == null) {
        items.set(
            index,
            new NotificationJob(
                item.id(),
                item.userId(),
                item.type(),
                item.title(),
                item.message(),
                item.actionTarget(),
                item.source(),
                item.createdBy(),
                item.createdAt(),
                readAt));
        updatedCount++;
      }
    }
    if (updatedCount > 0) {
      saveAll(userId, items);
    }
    return updatedCount;
  }

  @Override
  public boolean existsRecentByUserIdAndType(String userId, String type, Instant since) {
    return load(userId).stream()
        .anyMatch(
            item ->
                item.type().equalsIgnoreCase(type)
                    && item.createdAt() != null
                    && item.createdAt().isAfter(since));
  }

  private List<NotificationJob> load(String userId) {
    List<String> rows = redisTemplate.opsForList().range(key(userId), 0, -1);
    if (rows == null) {
      return List.of();
    }

    List<NotificationJob> result = new ArrayList<>();
    for (String row : rows) {
      try {
        result.add(objectMapper.readValue(row, NotificationJob.class));
      } catch (Exception ex) {
        throw new IllegalArgumentException("Could not parse notification");
      }
    }
    return result;
  }

  private void saveAll(String userId, List<NotificationJob> items) {
    try {
      var key = key(userId);
      redisTemplate.delete(key);
      for (var item : items) {
        redisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(item));
      }
    } catch (Exception ex) {
      throw new IllegalArgumentException("Could not save notification");
    }
  }

  private String key(String userId) {
    return "notification-jobs:" + userId;
  }

  private Comparator<NotificationJob> comparator(Pageable pageable) {
    var order = pageable.getSort().stream().findFirst().orElse(null);
    Comparator<NotificationJob> comparator;

    if (order == null || "createdAt".equals(order.getProperty())) {
      comparator = Comparator.comparing(NotificationJob::createdAt);
    } else if ("title".equals(order.getProperty())) {
      comparator = Comparator.comparing(NotificationJob::title, String.CASE_INSENSITIVE_ORDER);
    } else if ("type".equals(order.getProperty())) {
      comparator = Comparator.comparing(NotificationJob::type, String.CASE_INSENSITIVE_ORDER);
    } else {
      comparator = Comparator.comparing(NotificationJob::message, String.CASE_INSENSITIVE_ORDER);
    }

    return order != null && order.isAscending() ? comparator : comparator.reversed();
  }

  private boolean contains(String value, String search) {
    var source = value == null ? "" : value.toLowerCase(Locale.ROOT);
    return source.contains(search.toLowerCase(Locale.ROOT));
  }
}
