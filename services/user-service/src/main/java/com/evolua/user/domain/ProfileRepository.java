package com.evolua.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProfileRepository {
  Profile save(Profile item);

  Optional<Profile> findByUserId(String userId);

  Page<Profile> findAllByUserId(String userId, Pageable pageable, String search, Boolean premium);
}
