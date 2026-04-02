package com.evolua.chat.infrastructure.persistence;

import com.evolua.chat.domain.Message;
import com.evolua.chat.domain.MessageRepository;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MessagePersistenceAdapter implements MessageRepository {
  private final MessageMongoRepository repository;
  private final MongoTemplate mongoTemplate;

  public MessagePersistenceAdapter(MessageMongoRepository repository, MongoTemplate mongoTemplate) {
    this.repository = repository;
    this.mongoTemplate = mongoTemplate;
  }

  public Message save(Message item) {
    MessageDocument document = new MessageDocument();
    document.setId(item.id());
    document.setUserId(item.userId());
    document.setRecipientId(item.recipientId());
    document.setContent(item.content());
    document.setCreatedAt(item.createdAt());
    MessageDocument saved = repository.save(document);
    return new Message(saved.getId(), saved.getUserId(), saved.getRecipientId(), saved.getContent(), saved.getCreatedAt());
  }

  public Page<Message> findAllByUserId(
      String userId, Pageable pageable, String search, String recipientId) {
    Query query = new Query().addCriteria(Criteria.where("userId").is(userId));

    if (search != null && !search.isBlank()) {
      String regex = Pattern.quote(search);
      query.addCriteria(
          new Criteria()
              .orOperator(
                  Criteria.where("content").regex(regex, "i"),
                  Criteria.where("recipientId").regex(regex, "i")));
    }

    if (recipientId != null && !recipientId.isBlank()) {
      query.addCriteria(Criteria.where("recipientId").is(recipientId));
    }

    long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), MessageDocument.class);
    query.with(pageable);

    var items =
        mongoTemplate.find(query, MessageDocument.class).stream()
            .map(saved -> new Message(saved.getId(), saved.getUserId(), saved.getRecipientId(), saved.getContent(), saved.getCreatedAt()))
            .toList();

    return new PageImpl<>(items, pageable, total);
  }
}
