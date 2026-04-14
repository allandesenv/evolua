package com.evolua.social.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepository {
  Post save(Post item);

  Page<Post> findAllByUserId(
      String userId, Pageable pageable, String search, String community, String visibility, Boolean mine);
}
