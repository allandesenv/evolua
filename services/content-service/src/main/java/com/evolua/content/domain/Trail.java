package com.evolua.content.domain;

import java.time.Instant;
import java.util.List;

public record Trail(
    Long id,
    String userId,
    String title,
    String summary,
    String content,
    String category,
    Boolean premium,
    List<TrailMediaLink> mediaLinks,
    Instant createdAt) {}
