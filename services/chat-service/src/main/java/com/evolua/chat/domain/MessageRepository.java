package com.evolua.chat.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageRepository {
  Message save(Message item);

  Page<Message> findAllByUserId(String userId, Pageable pageable, String search, String recipientId);
}
