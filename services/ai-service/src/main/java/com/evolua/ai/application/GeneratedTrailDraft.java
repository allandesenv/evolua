package com.evolua.ai.application;

import java.util.List;

public record GeneratedTrailDraft(
    String title,
    String summary,
    String content,
    String category,
    String sourceStyle,
    List<GeneratedTrailMediaLink> mediaLinks) {}
