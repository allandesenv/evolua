package com.evolua.chat.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageMongoRepository extends MongoRepository<MessageDocument, String> {}
