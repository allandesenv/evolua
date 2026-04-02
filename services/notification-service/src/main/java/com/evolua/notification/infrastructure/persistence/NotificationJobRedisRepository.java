package com.evolua.notification.infrastructure.persistence;

import com.evolua.notification.domain.NotificationJob;
import com.evolua.notification.domain.NotificationJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

  public NotificationJob save(NotificationJob item) {
    try {
      redisTemplate.opsForList().rightPush("notification-jobs:" + item.userId(), objectMapper.writeValueAsString(item));
      return item;
    } catch (Exception ex) {
      throw new IllegalArgumentException("Could not save notification");
    }
  }

  public Page<NotificationJob> findAllByUserId(String userId, Pageable pageable, String search, String channel) {
    List<String> rows = redisTemplate.opsForList().range("notification-jobs:" + userId, 0, -1);
    if (rows == null) {
      return Page.empty(pageable);
    }

    List<NotificationJob> result = new ArrayList<>();
    for (String row : rows) {
      try {
        result.add(objectMapper.readValue(row, NotificationJob.class));
      } catch (Exception ex) {
        throw new IllegalArgumentException("Could not parse notification");
      }
    }

    var filtered =
        result.stream()
            .filter(
                item ->
                    search == null
                        || search.isBlank()
                        || item.message().toLowerCase().contains(search.toLowerCase())
                        || item.channel().toLowerCase().contains(search.toLowerCase()))
            .filter(item -> channel == null || channel.isBlank() || item.channel().equalsIgnoreCase(channel))
            .sorted(comparator(pageable))
            .toList();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), filtered.size());
    List<NotificationJob> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);

    return new PageImpl<>(pageContent, pageable, filtered.size());
  }

  private Comparator<NotificationJob> comparator(Pageable pageable) {
    var order = pageable.getSort().stream().findFirst().orElse(null);
    Comparator<NotificationJob> comparator;

    if (order == null || "createdAt".equals(order.getProperty())) {
      comparator = Comparator.comparing(NotificationJob::createdAt);
    } else if ("channel".equals(order.getProperty())) {
      comparator = Comparator.comparing(NotificationJob::channel, String.CASE_INSENSITIVE_ORDER);
    } else {
      comparator = Comparator.comparing(NotificationJob::message, String.CASE_INSENSITIVE_ORDER);
    }

    return order != null && order.isAscending() ? comparator : comparator.reversed();
  }
}
