package com.evolua.content.application;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailProgress;
import com.evolua.content.domain.TrailProgressRepository;
import com.evolua.content.domain.TrailRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrailJourneyService {
  private final TrailRepository trailRepository;
  private final TrailProgressRepository progressRepository;
  private final TrailStepDeriver stepDeriver;

  public TrailJourneyService(
      TrailRepository trailRepository,
      TrailProgressRepository progressRepository,
      TrailStepDeriver stepDeriver) {
    this.trailRepository = trailRepository;
    this.progressRepository = progressRepository;
    this.stepDeriver = stepDeriver;
  }

  public TrailJourney getJourney(String userId, Long trailId) {
    var trail = findAccessibleTrail(userId, trailId);
    var progress = findProgress(userId, trailId);
    var steps = withStatus(stepDeriver.derive(trail), progress, progress != null);
    return buildJourney(trail, steps, progress);
  }

  public TrailJourney startJourney(String userId, Long trailId) {
    var trail = findAccessibleTrail(userId, trailId);
    var progress = findProgress(userId, trailId);
    if (progress == null) {
      var now = Instant.now();
      progress =
          progressRepository.save(
              new TrailProgress(null, userId, trailId, 0, List.of(), now, now, null));
    }
    var steps = withStatus(stepDeriver.derive(trail), progress, true);
    return buildJourney(trail, steps, progress);
  }

  public TrailJourney completeStep(String userId, Long trailId, Integer stepIndex) {
    var trail = findAccessibleTrail(userId, trailId);
    var baseSteps = stepDeriver.derive(trail);
    if (stepIndex == null || stepIndex < 0 || stepIndex >= baseSteps.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Etapa invalida.");
    }

    var progress = findProgress(userId, trailId);
    if (progress == null) {
      var now = Instant.now();
      progress = new TrailProgress(null, userId, trailId, 0, List.of(), now, now, null);
    }

    var completed = new ArrayList<>(progress.completedStepIndexes());
    if (!completed.contains(stepIndex)) {
      completed.add(stepIndex);
    }
    completed.sort(Comparator.naturalOrder());

    var nextIndex = nextOpenIndex(baseSteps.size(), completed);
    var now = Instant.now();
    var completedAt = completed.size() >= baseSteps.size() ? now : null;
    var saved =
        progressRepository.save(
            new TrailProgress(
                progress.id(),
                userId,
                trailId,
                nextIndex,
                completed,
                progress.startedAt(),
                now,
                completedAt));
    var steps = withStatus(baseSteps, saved, true);
    return buildJourney(trail, steps, saved);
  }

  private Trail findAccessibleTrail(String userId, Long trailId) {
    var trail = trailRepository.findById(trailId);
    if (trail == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada.");
    }
    if (Boolean.TRUE.equals(trail.privateTrail()) && !userId.equals(trail.userId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada.");
    }
    return trail;
  }

  private TrailProgress findProgress(String userId, Long trailId) {
    return progressRepository.findByUserIdAndTrailId(userId, trailId);
  }

  private List<TrailJourneyStep> withStatus(
      List<TrailJourneyStep> steps, TrailProgress progress, boolean started) {
    var completed = progress == null ? List.<Integer>of() : progress.completedStepIndexes();
    var current = progress == null ? 0 : progress.currentStepIndex();
    var allDone = progress != null && progress.completedAt() != null;
    var result = new ArrayList<TrailJourneyStep>();
    for (var step : steps) {
      var status =
          completed.contains(step.index())
              ? "completed"
              : allDone
                  ? "completed"
                  : started && step.index().equals(current) ? "current" : "upcoming";
      result.add(
          new TrailJourneyStep(
              step.index(),
              step.title(),
              step.summary(),
              step.content(),
              status,
              step.estimatedMinutes(),
              step.mediaLinks()));
    }
    return result;
  }

  private TrailJourney buildJourney(Trail trail, List<TrailJourneyStep> steps, TrailProgress progress) {
    var completedCount =
        progress == null
            ? 0
            : (int) progress.completedStepIndexes().stream().filter(index -> index < steps.size()).count();
    var percent = steps.isEmpty() ? 0 : Math.round((completedCount * 100f) / steps.size());
    var next =
        steps.stream()
            .filter(step -> !"completed".equals(step.status()))
            .findFirst()
            .orElse(steps.isEmpty() ? null : steps.get(steps.size() - 1));
    return new TrailJourney(trail, steps, progress, percent, next);
  }

  private Integer nextOpenIndex(int stepCount, List<Integer> completed) {
    for (int index = 0; index < stepCount; index++) {
      if (!completed.contains(index)) {
        return index;
      }
    }
    return Math.max(0, stepCount - 1);
  }
}
