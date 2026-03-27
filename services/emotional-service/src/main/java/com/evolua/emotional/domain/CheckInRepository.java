package com.evolua.emotional.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CheckInRepository {
  CheckIn save(CheckIn item);

  Page<CheckIn> findAllByUserId(String userId, Pageable pageable, String search, String mood);
}
