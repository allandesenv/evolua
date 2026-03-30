package com.evolua.social.application;

import com.evolua.social.domain.Community;
import com.evolua.social.domain.CommunityRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommunityService {
  private final CommunityRepository repository;

  public CommunityService(CommunityRepository repository) {
    this.repository = repository;
  }

  public Community create(
      String userId,
      String slug,
      String name,
      String description,
      String visibility,
      String category) {
    String normalizedSlug = normalizeSlug(slug, name);
    repository.findBySlug(normalizedSlug).ifPresent(existing -> {
      throw new IllegalArgumentException("Community slug already exists");
    });

    return repository.save(
        new Community(
            null,
            normalizedSlug,
            name.trim(),
            description == null ? "" : description.trim(),
            normalizeVisibility(visibility),
            normalizeCategory(category),
            List.of(userId),
            Instant.now()));
  }

  public Page<Community> list(
      String userId,
      Pageable pageable,
      String search,
      String visibility,
      String category,
      Boolean joined) {
    return repository.findAll(
        userId,
        pageable,
        search,
        visibility == null ? null : normalizeVisibility(visibility),
        category == null ? null : normalizeCategory(category),
        joined);
  }

  public Community join(String userId, String id) {
    Community current = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Community not found"));
    var members = new LinkedHashSet<>(current.memberIds());
    members.add(userId);
    return repository.save(
        new Community(
            current.id(),
            current.slug(),
            current.name(),
            current.description(),
            current.visibility(),
            current.category(),
            new ArrayList<>(members),
            current.createdAt()));
  }

  public Community leave(String userId, String id) {
    Community current = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Community not found"));
    var members = new ArrayList<>(current.memberIds());
    members.removeIf(memberId -> memberId.equals(userId));
    return repository.save(
        new Community(
            current.id(),
            current.slug(),
            current.name(),
            current.description(),
            current.visibility(),
            current.category(),
            members,
            current.createdAt()));
  }

  public Community requireBySlug(String slug) {
    return repository.findBySlug(normalizeSlug(slug, slug)).orElseThrow(() -> new IllegalArgumentException("Community not found"));
  }

  private String normalizeVisibility(String visibility) {
    String normalized = visibility == null ? "PUBLIC" : visibility.trim().toUpperCase(Locale.ROOT);
    if (!normalized.equals("PUBLIC") && !normalized.equals("PRIVATE")) {
      throw new IllegalArgumentException("Community visibility must be PUBLIC or PRIVATE");
    }
    return normalized;
  }

  private String normalizeCategory(String category) {
    if (category == null || category.isBlank()) {
      return "geral";
    }
    return category.trim().toLowerCase(Locale.ROOT);
  }

  private String normalizeSlug(String slug, String fallbackName) {
    String source = (slug == null || slug.isBlank()) ? fallbackName : slug;
    String normalized =
        source
            .toLowerCase(Locale.ROOT)
            .trim()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    if (normalized.isBlank()) {
      normalized = "community-" + UUID.randomUUID().toString().substring(0, 8);
    }
    return normalized;
  }
}
