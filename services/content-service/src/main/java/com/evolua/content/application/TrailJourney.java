package com.evolua.content.application;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailProgress;
import java.util.List;

public record TrailJourney(
    Trail trail,
    List<TrailJourneyStep> steps,
    TrailProgress progress,
    Integer progressPercent,
    TrailJourneyStep nextStep) {}
