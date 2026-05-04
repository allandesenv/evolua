package com.evolua.content.infrastructure.persistence;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailMediaLink;
import com.evolua.content.domain.TrailRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class TrailPersistenceAdapter implements TrailRepository {
  private static final TypeReference<List<TrailMediaLink>> MEDIA_LINKS_TYPE = new TypeReference<>() {};

  private final TrailJpaRepository repository;
  private final ObjectMapper objectMapper;

  public TrailPersistenceAdapter(TrailJpaRepository repository, ObjectMapper objectMapper) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  public Trail save(Trail item) {
    TrailEntity entity = new TrailEntity();
    entity.setId(item.id());
    entity.setUserId(item.userId());
    entity.setTitle(item.title());
    entity.setDescription(item.summary());
    entity.setSummary(item.summary());
    entity.setContent(item.content());
    entity.setCategory(item.category());
    entity.setPremium(item.premium());
    entity.setPrivateTrail(Boolean.TRUE.equals(item.privateTrail()));
    entity.setActiveJourney(Boolean.TRUE.equals(item.activeJourney()));
    entity.setGeneratedByAi(Boolean.TRUE.equals(item.generatedByAi()));
    entity.setJourneyKey(item.journeyKey());
    entity.setSourceStyle(item.sourceStyle());
    entity.setMediaLinks(serializeMediaLinks(item.mediaLinks()));
    entity.setCreatedAt(item.createdAt());
    return toDomain(repository.save(entity));
  }

  @Override
  public Trail findById(Long id) {
    return repository.findById(id).map(this::toDomain).orElse(null);
  }

  @Override
  public void deleteById(Long id) {
    repository.deleteById(id);
  }

  public Page<Trail> findAll(
      String userId, Pageable pageable, String search, String category, Boolean premium) {
    Specification<TrailEntity> specification = Specification.where(null);

    specification = specification.and((root, query, cb) -> cb.isFalse(root.get("privateTrail")));

    if (search != null && !search.isBlank()) {
      var normalized = "%" + search.toLowerCase() + "%";
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("title")), normalized),
                      cb.like(cb.lower(root.get("summary")), normalized),
                      cb.like(cb.lower(root.get("content")), normalized),
                      cb.like(cb.lower(root.get("category")), normalized)));
    }

    if (category != null && !category.isBlank()) {
      var normalizedCategory = category.toLowerCase();
      specification =
          specification.and(
              (root, query, cb) -> cb.equal(cb.lower(root.get("category")), normalizedCategory));
    }

    if (premium != null) {
      specification = specification.and((root, query, cb) -> cb.equal(root.get("premium"), premium));
    }

    return repository.findAll(specification, pageable).map(this::toDomain);
  }

  @Override
  public Trail findActiveJourneyByUserId(String userId) {
    return repository.findFirstByUserIdAndActiveJourneyTrueOrderByCreatedAtDesc(userId)
        .map(this::toDomain)
        .orElse(null);
  }

  @Override
  public Trail findActiveJourneyByUserIdAndJourneyKey(String userId, String journeyKey) {
    if (journeyKey == null || journeyKey.isBlank()) {
      return null;
    }

    return repository.findFirstByUserIdAndJourneyKeyAndActiveJourneyTrueOrderByCreatedAtDesc(
            userId, journeyKey)
        .map(this::toDomain)
        .orElse(null);
  }

  @Override
  public void deactivateActiveJourneys(String userId) {
    repository.deactivateActiveJourneys(userId);
  }

  private Trail toDomain(TrailEntity saved) {
    return new Trail(
        saved.getId(),
        saved.getUserId(),
        saved.getTitle(),
        saved.getSummary() == null || saved.getSummary().isBlank() ? saved.getDescription() : saved.getSummary(),
        saved.getContent() == null || saved.getContent().isBlank() ? saved.getDescription() : saved.getContent(),
        saved.getCategory(),
        saved.getPremium(),
        Optional.ofNullable(saved.getPrivateTrail()).orElse(Boolean.FALSE),
        Optional.ofNullable(saved.getActiveJourney()).orElse(Boolean.FALSE),
        Optional.ofNullable(saved.getGeneratedByAi()).orElse(Boolean.FALSE),
        saved.getJourneyKey(),
        saved.getSourceStyle(),
        deserializeMediaLinks(saved.getMediaLinks()),
        saved.getCreatedAt());
  }

  private String serializeMediaLinks(List<TrailMediaLink> mediaLinks) {
    try {
      return objectMapper.writeValueAsString(mediaLinks == null ? List.of() : mediaLinks);
    } catch (Exception exception) {
      throw new IllegalArgumentException("Could not serialize trail media links");
    }
  }

  private List<TrailMediaLink> deserializeMediaLinks(String mediaLinks) {
    try {
      if (mediaLinks == null || mediaLinks.isBlank()) {
        return List.of();
      }
      return objectMapper.readValue(mediaLinks, MEDIA_LINKS_TYPE);
    } catch (Exception exception) {
      return List.of();
    }
  }
}
