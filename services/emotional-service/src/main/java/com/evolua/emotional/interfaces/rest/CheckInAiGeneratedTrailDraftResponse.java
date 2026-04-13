package com.evolua.emotional.interfaces.rest;

import java.util.List;

public record CheckInAiGeneratedTrailDraftResponse(
    String title,
    String summary,
    String content,
    String category,
    String sourceStyle,
    List<CheckInAiGeneratedTrailDraftLinkResponse> mediaLinks) {}
