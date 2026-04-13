package com.evolua.content.application;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailMediaLink;
import com.evolua.content.domain.TrailRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TrailService {
  private final TrailRepository repository;

  public TrailService(TrailRepository repository) {
    this.repository = repository;
  }

  public Trail create(
      String userId,
      String title,
      String summary,
      String content,
      String category,
      Boolean premium,
      List<TrailMediaLink> mediaLinks) {
    return repository.save(
        new Trail(
            null,
            userId,
            title,
            summary,
            content,
            category,
            premium,
            false,
            false,
            false,
            null,
            null,
            mediaLinks,
            Instant.now()));
  }

  public Page<Trail> list(
      String userId, Pageable pageable, String search, String category, Boolean premium) {
    return repository.findAll(userId, pageable, search, category, premium);
  }

  public Trail currentJourney(String userId) {
    return repository.findActiveJourneyByUserId(userId);
  }

  public Trail upsertJourneyTrail(
      String userId,
      String title,
      String summary,
      String content,
      String category,
      List<TrailMediaLink> mediaLinks,
      String journeyKey,
      String sourceStyle) {
    var existing = repository.findActiveJourneyByUserIdAndJourneyKey(userId, journeyKey);
    if (existing != null) {
      return repository.save(
          new Trail(
              existing.id(),
              userId,
              title,
              summary,
              content,
              category,
              false,
              true,
              true,
              true,
              journeyKey,
              sourceStyle,
              mediaLinks,
              existing.createdAt()));
    }

    repository.deactivateActiveJourneys(userId);
    return repository.save(
        new Trail(
            null,
            userId,
            title,
            summary,
            content,
            category,
            false,
            true,
            true,
            true,
            journeyKey,
            sourceStyle,
            mediaLinks,
            Instant.now()));
  }
}
