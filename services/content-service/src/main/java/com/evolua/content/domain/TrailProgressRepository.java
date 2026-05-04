package com.evolua.content.domain;

public interface TrailProgressRepository {
  TrailProgress save(TrailProgress progress);

  TrailProgress findByUserIdAndTrailId(String userId, Long trailId);
}
