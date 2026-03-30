package com.evolua.social.interfaces.rest;

import java.time.Instant;

public record CommunityResponse(
    String id,
    String slug,
    String name,
    String description,
    String visibility,
    String category,
    int memberCount,
    boolean joined,
    Instant createdAt) {}
