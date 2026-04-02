package com.evolua.content.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrailRepository {
  Trail save(Trail item);

  Page<Trail> findAll(Pageable pageable, String search, String category, Boolean premium);
}
