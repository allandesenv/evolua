package com.evolua.social.domain;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunityRepository {
  Community save(Community item);

  Optional<Community> findById(String id);

  Optional<Community> findBySlug(String slug);

  Page<Community> findAll(
      String userId,
      Pageable pageable,
      String search,
      String visibility,
      String category,
      Boolean joined);
}
