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
  private final SubscriptionAccessClient subscriptionAccessClient;

  public CheckInService(CheckInRepository repository, SubscriptionAccessClient subscriptionAccessClient) {
    this.repository = repository;
    this.subscriptionAccessClient = subscriptionAccessClient;
  }

  public CheckIn create(
      String userId, String mood, String reflection, Integer energyLevel, String recommendedPractice) {
    return create(
        userId,
        mood,
        reflection,
        energyLevel,
        recommendedPractice,
        mood,
        null,
        null,
        null,
        null,
        null);
  }

  public CheckIn create(
      String userId,
      String mood,
      String reflection,
      Integer energyLevel,
      String recommendedPractice,
      String emotion,
      Integer intensity,
      String energy,
      String context,
      String decisionTags,
      String severityLevel) {
    return repository.save(
        new CheckIn(
            null,
            userId,
            mood,
            reflection == null ? "" : reflection,
            energyLevel,
            recommendedPractice,
            Instant.now(),
            emotion,
            intensity,
            energy,
            context,
            decisionTags,
            severityLevel));
  }

  public CheckIn updateRecommendedPractice(CheckIn checkIn, String recommendedPractice) {
    return repository.save(
        new CheckIn(
            checkIn.id(),
            checkIn.userId(),
            checkIn.mood(),
            checkIn.reflection(),
            checkIn.energyLevel(),
            recommendedPractice,
            checkIn.createdAt(),
            checkIn.emotion(),
            checkIn.intensity(),
            checkIn.energy(),
            checkIn.context(),
            checkIn.decisionTags(),
            checkIn.severityLevel()));
  }

  public java.util.List<CheckIn> recentSince(String userId, Instant from) {
    return repository.findRecentByUserId(userId, from);
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
    var effectiveFrom = from;
    if (!subscriptionAccessClient.hasPremiumAccess(userId)) {
      var freeHistoryFloor = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
      if (effectiveFrom == null || effectiveFrom.isBefore(freeHistoryFloor)) {
        effectiveFrom = freeHistoryFloor;
      }
    }
    return repository.findAllByUserId(userId, pageable, search, mood, energyMin, energyMax, effectiveFrom, to);
  }
}
