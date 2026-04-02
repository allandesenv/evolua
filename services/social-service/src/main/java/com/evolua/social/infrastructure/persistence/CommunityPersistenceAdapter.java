package com.evolua.social.infrastructure.persistence;

import com.evolua.social.domain.Community;
import com.evolua.social.domain.CommunityRepository;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityPersistenceAdapter implements CommunityRepository {
  private final CommunityMongoRepository repository;
  private final MongoTemplate mongoTemplate;

  public CommunityPersistenceAdapter(CommunityMongoRepository repository, MongoTemplate mongoTemplate) {
    this.repository = repository;
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Community save(Community item) {
    CommunityDocument document = new CommunityDocument();
    document.setId(item.id());
    document.setSlug(item.slug());
    document.setName(item.name());
    document.setDescription(item.description());
    document.setVisibility(item.visibility());
    document.setCategory(item.category());
    document.setMemberIds(item.memberIds());
    document.setCreatedAt(item.createdAt());
    return toDomain(repository.save(document));
  }

  @Override
  public Optional<Community> findById(String id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Community> findBySlug(String slug) {
    Query query = new Query().addCriteria(Criteria.where("slug").regex("^" + Pattern.quote(slug) + "$", "i"));
    return Optional.ofNullable(mongoTemplate.findOne(query, CommunityDocument.class)).map(this::toDomain);
  }

  @Override
  public Page<Community> findAll(
      String userId,
      Pageable pageable,
      String search,
      String visibility,
      String category,
      Boolean joined) {
    Query query = new Query();

    if (search != null && !search.isBlank()) {
      String regex = Pattern.quote(search);
      query.addCriteria(
          new Criteria()
              .orOperator(
                  Criteria.where("name").regex(regex, "i"),
                  Criteria.where("slug").regex(regex, "i"),
                  Criteria.where("description").regex(regex, "i"),
                  Criteria.where("category").regex(regex, "i")));
    }

    if (visibility != null && !visibility.isBlank()) {
      query.addCriteria(Criteria.where("visibility").is(visibility));
    }

    if (category != null && !category.isBlank()) {
      query.addCriteria(Criteria.where("category").regex("^" + Pattern.quote(category) + "$", "i"));
    }

    if (Boolean.TRUE.equals(joined)) {
      query.addCriteria(Criteria.where("memberIds").is(userId));
    } else if (Boolean.FALSE.equals(joined)) {
      query.addCriteria(Criteria.where("memberIds").nin(userId));
    }

    long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), CommunityDocument.class);
    query.with(pageable);

    List<Community> items =
        mongoTemplate.find(query, CommunityDocument.class).stream().map(this::toDomain).toList();
    return new PageImpl<>(items, pageable, total);
  }

  private Community toDomain(CommunityDocument document) {
    return new Community(
        document.getId(),
        document.getSlug(),
        document.getName(),
        document.getDescription(),
        document.getVisibility(),
        document.getCategory(),
        document.getMemberIds() == null ? List.of() : List.copyOf(document.getMemberIds()),
        document.getCreatedAt());
  }
}
