package com.evolua.user.application;

import com.evolua.user.domain.Profile;
import com.evolua.user.domain.ProfileRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
  private final ProfileRepository repository;

  public ProfileService(ProfileRepository repository) {
    this.repository = repository;
  }

  public Profile create(String userId, String displayName, String bio, Integer journeyLevel, Boolean premium) {
    return repository.save(new Profile(null, userId, displayName, bio, journeyLevel, premium, Instant.now()));
  }

  public Page<Profile> list(String userId, Pageable pageable, String search, Boolean premium) {
    return repository.findAllByUserId(userId, pageable, search, premium);
  }
}
