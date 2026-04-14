package com.evolua.ai.application;

public record SpaceCandidate(
    String id,
    String slug,
    String name,
    String description,
    String category,
    String visibility,
    Boolean joined) {}
