package com.evolua.content.application;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TrailService {
  private final TrailRepository repository;

  public TrailService(TrailRepository repository) {
    this.repository = repository;
  }

  public Trail create(String userId, String title, String description, String category, Boolean premium) {
    return repository.save(new Trail(null, userId, title, description, category, premium, Instant.now()));
  }

  public Page<Trail> list(String userId, Pageable pageable, String search, String category, Boolean premium) {
    return repository.findAllByUserId(userId, pageable, search, category, premium);
  }
}
