package com.evolua.social.domain;

import java.time.Instant;
import java.util.List;

public record Community(
    String id,
    String slug,
    String name,
    String description,
    String visibility,
    String category,
    List<String> memberIds,
    Instant createdAt) {}
