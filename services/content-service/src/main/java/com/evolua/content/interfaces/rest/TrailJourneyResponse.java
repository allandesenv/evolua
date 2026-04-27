package com.evolua.content.interfaces.rest;

import java.util.List;

public record TrailJourneyResponse(
    TrailResponse trail,
    List<TrailJourneyStepResponse> steps,
    TrailProgressResponse progress,
    Integer progressPercent,
    TrailJourneyStepResponse nextStep) {}
