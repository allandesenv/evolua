package com.evolua.ai.application;

public record TrailCandidate(
    Long id,
    String title,
    String summary,
    String category,
    Boolean premium,
    Boolean accessible) {}
