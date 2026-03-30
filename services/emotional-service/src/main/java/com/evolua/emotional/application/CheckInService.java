package com.evolua.emotional.application;

import com.evolua.emotional.domain.CheckIn;
import com.evolua.emotional.domain.CheckInRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CheckInService {
  private final CheckInRepository repository;

  public CheckInService(CheckInRepository repository) {
    this.repository = repository;
  }

  public CheckIn create(
      String userId, String mood, String reflection, Integer energyLevel, String recommendedPractice) {
    return repository.save(new CheckIn(null, userId, mood, reflection, energyLevel, recommendedPractice, Instant.now()));
  }

  public Page<CheckIn> list(
      String userId,
      Pageable pageable,
      String search,
      String mood,
      Integer energyMin,
      Integer energyMax,
      Instant from,
      Instant to) {
    return repository.findAllByUserId(userId, pageable, search, mood, energyMin, energyMax, from, to);
  }
}
