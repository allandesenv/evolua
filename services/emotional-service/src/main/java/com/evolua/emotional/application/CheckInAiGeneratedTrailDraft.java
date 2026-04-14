package com.evolua.emotional.application;

import java.util.List;

public record CheckInAiGeneratedTrailDraft(
    String title,
    String summary,
    String content,
    String category,
    String sourceStyle,
    List<CheckInAiGeneratedTrailDraftLink> mediaLinks) {}
