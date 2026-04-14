package com.evolua.social.infrastructure.persistence;

import com.evolua.social.domain.Post;
import com.evolua.social.domain.PostRepository;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class PostPersistenceAdapter implements PostRepository {
  private final PostMongoRepository repository;
  private final MongoTemplate mongoTemplate;

  public PostPersistenceAdapter(PostMongoRepository repository, MongoTemplate mongoTemplate) {
    this.repository = repository;
    this.mongoTemplate = mongoTemplate;
  }

  public Post save(Post item) {
    PostDocument document = new PostDocument();
    document.setId(item.id());
    document.setUserId(item.userId());
    document.setContent(item.content());
    document.setCommunity(item.community());
    document.setVisibility(item.visibility());
    document.setCreatedAt(item.createdAt());
    PostDocument saved = repository.save(document);
    return new Post(saved.getId(), saved.getUserId(), saved.getContent(), saved.getCommunity(), saved.getVisibility(), saved.getCreatedAt());
  }

  public Page<Post> findAllByUserId(
      String userId, Pageable pageable, String search, String community, String visibility, Boolean mine) {
    Query query = new Query();

    if (Boolean.TRUE.equals(mine)) {
      query.addCriteria(Criteria.where("userId").is(userId));
    } else {
      query.addCriteria(
          new Criteria()
              .orOperator(
                  Criteria.where("visibility").is("PUBLIC"),
                  Criteria.where("userId").is(userId)));
    }

    if (search != null && !search.isBlank()) {
      String regex = Pattern.quote(search);
      query.addCriteria(
          new Criteria()
              .orOperator(
                  Criteria.where("content").regex(regex, "i"),
                  Criteria.where("community").regex(regex, "i")));
    }

    if (community != null && !community.isBlank()) {
      query.addCriteria(Criteria.where("community").regex("^" + Pattern.quote(community) + "$", "i"));
    }

    if (visibility != null && !visibility.isBlank()) {
      if (Boolean.TRUE.equals(mine)) {
        query.addCriteria(Criteria.where("visibility").is(visibility));
      } else if ("PRIVATE".equalsIgnoreCase(visibility)) {
        query.addCriteria(
            new Criteria()
                .andOperator(
                    Criteria.where("userId").is(userId),
                    Criteria.where("visibility").is("PRIVATE")));
      } else {
        query.addCriteria(Criteria.where("visibility").is(visibility));
      }
    }

    long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), PostDocument.class);
    query.with(pageable);

    var items =
        mongoTemplate.find(query, PostDocument.class).stream()
            .map(saved -> new Post(saved.getId(), saved.getUserId(), saved.getContent(), saved.getCommunity(), saved.getVisibility(), saved.getCreatedAt()))
            .toList();

    return new PageImpl<>(items, pageable, total);
  }
}
