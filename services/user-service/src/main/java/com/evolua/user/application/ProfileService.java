package com.evolua.user.application;

import com.evolua.user.domain.Profile;
import com.evolua.user.domain.ProfileRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
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
    return repository.save(
        new Profile(
            null,
            userId,
            displayName,
            normalizeBio(bio),
            normalizeJourneyLevel(journeyLevel),
            premium,
            null,
            null,
            null,
            null,
            Instant.now()));
  }

  public Optional<Profile> findByUserId(String userId) {
    return repository.findByUserId(userId);
  }

  public Profile upsertMe(
      String userId,
      String displayName,
      String bio,
      Integer journeyLevel,
      LocalDate birthDate,
      String gender,
      String customGender) {
    var normalizedGender = normalizeGender(gender);
    var normalizedCustomGender = normalizeCustomGender(normalizedGender, customGender);
    var existing = repository.findByUserId(userId).orElse(null);

    if (existing == null) {
      return repository.save(
          new Profile(
              null,
              userId,
              displayName,
              normalizeBio(bio),
              normalizeJourneyLevel(journeyLevel),
              false,
              birthDate,
              normalizedGender,
              normalizedCustomGender,
              null,
              Instant.now()));
    }

    return repository.save(
        new Profile(
            existing.id(),
            existing.userId(),
            displayName,
            normalizeBio(bio),
            normalizeJourneyLevel(journeyLevel),
            existing.premium(),
            birthDate,
            normalizedGender,
            normalizedCustomGender,
            existing.avatarUrl(),
            existing.createdAt()));
  }

  public Profile updateAvatar(String userId, String avatarUrl) {
    var existing =
        repository
            .findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
    return repository.save(
        new Profile(
            existing.id(),
            existing.userId(),
            existing.displayName(),
            existing.bio(),
            existing.journeyLevel(),
            existing.premium(),
            existing.birthDate(),
            existing.gender(),
            existing.customGender(),
            avatarUrl,
            existing.createdAt()));
  }

  public Page<Profile> list(String userId, Pageable pageable, String search, Boolean premium) {
    return repository.findAllByUserId(userId, pageable, search, premium);
  }

  private String normalizeBio(String bio) {
    return bio == null ? "" : bio.trim();
  }

  private Integer normalizeJourneyLevel(Integer journeyLevel) {
    return journeyLevel == null ? 1 : Math.max(1, Math.min(10, journeyLevel));
  }

  private String normalizeGender(String gender) {
    if (gender == null || gender.isBlank()) {
      throw new IllegalArgumentException("Gender is required");
    }

    return switch (gender.trim().toUpperCase()) {
      case "MALE", "FEMALE", "CUSTOM" -> gender.trim().toUpperCase();
      default -> throw new IllegalArgumentException("Invalid gender");
    };
  }

  private String normalizeCustomGender(String gender, String customGender) {
    if (!"CUSTOM".equals(gender)) {
      return null;
    }
    if (customGender == null || customGender.isBlank()) {
      throw new IllegalArgumentException("Custom gender is required");
    }
    return customGender.trim();
  }
}
