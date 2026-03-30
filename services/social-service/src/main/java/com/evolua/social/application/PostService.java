package com.evolua.social.application;

import com.evolua.social.domain.Post;
import com.evolua.social.domain.CommunityRepository;
import com.evolua.social.domain.PostRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostService {
  private final PostRepository repository;
  private final CommunityRepository communityRepository;

  public PostService(PostRepository repository, CommunityRepository communityRepository) {
    this.repository = repository;
    this.communityRepository = communityRepository;
  }

  public Post create(String userId, String content, String community, String visibility) {
    communityRepository.findBySlug(community.trim()).orElseThrow(() -> new IllegalArgumentException("Community not found"));
    return repository.save(new Post(null, userId, content, community, visibility, Instant.now()));
  }

  public Page<Post> list(
      String userId, Pageable pageable, String search, String community, String visibility) {
    return repository.findAllByUserId(userId, pageable, search, community, visibility);
  }
}
