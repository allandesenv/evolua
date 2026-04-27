package com.evolua.content.interfaces.rest;

import java.util.List;

public record TrailJourneyStepResponse(
    Integer index,
    String title,
    String summary,
    String content,
    String status,
    Integer estimatedMinutes,
    List<TrailMediaLinkResponse> mediaLinks) {}
