package com.evolua.chat.application;

import com.evolua.chat.domain.Message;
import com.evolua.chat.domain.MessageRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
  private final MessageRepository repository;

  public MessageService(MessageRepository repository) {
    this.repository = repository;
  }

  public Message create(String userId, String recipientId, String content) {
    return repository.save(new Message(null, userId, recipientId, content, Instant.now()));
  }

  public Page<Message> list(String userId, Pageable pageable, String search, String recipientId) {
    return repository.findAllByUserId(userId, pageable, search, recipientId);
  }
}
