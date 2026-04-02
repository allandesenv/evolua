package com.evolua.social.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommunityMongoRepository extends MongoRepository<CommunityDocument, String> {
  Optional<CommunityDocument> findBySlug(String slug);
}
