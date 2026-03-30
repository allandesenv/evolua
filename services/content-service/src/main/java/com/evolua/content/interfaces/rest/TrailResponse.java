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
    Boolean accessible,
    List<TrailMediaLinkResponse> mediaLinks,
    Instant createdAt) {}
