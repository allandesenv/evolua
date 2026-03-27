package com.evolua.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfileRepository {
  Profile save(Profile item);

  Page<Profile> findAllByUserId(String userId, Pageable pageable, String search, Boolean premium);
}
