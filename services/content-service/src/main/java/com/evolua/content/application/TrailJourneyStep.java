package com.evolua.content.application;

import com.evolua.content.domain.TrailMediaLink;
import java.util.List;

public record TrailJourneyStep(
    Integer index,
    String title,
    String summary,
    String content,
    String status,
    Integer estimatedMinutes,
    List<TrailMediaLink> mediaLinks) {}
