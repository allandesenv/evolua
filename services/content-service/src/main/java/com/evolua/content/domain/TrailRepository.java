package com.evolua.content.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrailRepository {
  Trail save(Trail item);

  Trail findById(Long id);

  void deleteById(Long id);

  Page<Trail> findAll(
      String userId, Pageable pageable, String search, String category, Boolean premium);

  Trail findActiveJourneyByUserId(String userId);

  Trail findActiveJourneyByUserIdAndJourneyKey(String userId, String journeyKey);

  void deactivateActiveJourneys(String userId);
}
