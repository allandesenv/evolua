package com.evolua.social.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostMongoRepository extends MongoRepository<PostDocument, String> {}
