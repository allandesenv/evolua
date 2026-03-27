package com.evolua.content.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrailRepository {
  Trail save(Trail item);

  Page<Trail> findAllByUserId(String userId, Pageable pageable, String search, String category, Boolean premium);
}
