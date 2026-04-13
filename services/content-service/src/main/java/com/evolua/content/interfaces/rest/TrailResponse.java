package com.evolua.content.interfaces.rest;

import java.time.Instant;
import java.util.List;

public record TrailResponse(
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
    Boolean accessible,
    List<TrailMediaLinkResponse> mediaLinks,
    Instant createdAt) {}
