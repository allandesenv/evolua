package com.evolua.emotional.domain;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CheckInRepository {
  CheckIn save(CheckIn item);

  Page<CheckIn> findAllByUserId(
      String userId,
      Pageable pageable,
      String search,
      String mood,
      Integer energyMin,
      Integer energyMax,
      Instant from,
      Instant to);
}
