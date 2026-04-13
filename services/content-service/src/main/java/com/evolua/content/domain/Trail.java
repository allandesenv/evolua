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
    Boolean privateTrail,
    Boolean activeJourney,
    Boolean generatedByAi,
    String journeyKey,
    String sourceStyle,
    List<TrailMediaLink> mediaLinks,
    Instant createdAt) {}
