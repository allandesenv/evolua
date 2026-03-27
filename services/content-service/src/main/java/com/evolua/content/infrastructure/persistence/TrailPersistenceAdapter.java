package com.evolua.content.infrastructure.persistence;

import com.evolua.content.domain.Trail;
import com.evolua.content.domain.TrailRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class TrailPersistenceAdapter implements TrailRepository {
  private final TrailJpaRepository repository;

  public TrailPersistenceAdapter(TrailJpaRepository repository) {
    this.repository = repository;
  }

  public Trail save(Trail item) {
    TrailEntity entity = new TrailEntity();
    entity.setId(item.id());
    entity.setUserId(item.userId());
    entity.setTitle(item.title());
    entity.setDescription(item.description());
    entity.setCategory(item.category());
    entity.setPremium(item.premium());
    entity.setCreatedAt(item.createdAt());
    TrailEntity saved = repository.save(entity);
    return new Trail(
        saved.getId(),
        saved.getUserId(),
        saved.getTitle(),
        saved.getDescription(),
        saved.getCategory(),
        saved.getPremium(),
        saved.getCreatedAt());
  }

  public Page<Trail> findAllByUserId(
      String userId, Pageable pageable, String search, String category, Boolean premium) {
    Specification<TrailEntity> specification =
        Specification.where((root, query, cb) -> cb.equal(root.get("userId"), userId));

    if (search != null && !search.isBlank()) {
      var normalized = "%" + search.toLowerCase() + "%";
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("title")), normalized),
                      cb.like(cb.lower(root.get("description")), normalized),
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

    return repository.findAll(specification, pageable)
        .map(
            saved ->
                new Trail(
                    saved.getId(),
                    saved.getUserId(),
                    saved.getTitle(),
                    saved.getDescription(),
                    saved.getCategory(),
                    saved.getPremium(),
                    saved.getCreatedAt()));
  }
}
