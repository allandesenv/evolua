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
        new Trail(null, userId, title, summary, content, category, premium, mediaLinks, Instant.now()));
  }

  public Page<Trail> list(Pageable pageable, String search, String category, Boolean premium) {
    return repository.findAll(pageable, search, category, premium);
  }
}
