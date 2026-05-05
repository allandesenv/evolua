package com.evolua.user.infrastructure.persistence;

import com.evolua.user.domain.SupportTicket;
import com.evolua.user.domain.SupportTicketRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SupportTicketPersistenceAdapter implements SupportTicketRepository {
  private final SupportTicketJpaRepository repository;

  public SupportTicketPersistenceAdapter(SupportTicketJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public SupportTicket save(SupportTicket ticket) {
    var entity = new SupportTicketEntity();
    entity.setId(ticket.id());
    entity.setUserId(ticket.userId());
    entity.setEmail(ticket.email());
    entity.setCategory(ticket.category());
    entity.setSubject(ticket.subject());
    entity.setMessage(ticket.message());
    entity.setStatus(ticket.status());
    entity.setCreatedAt(ticket.createdAt());
    entity.setUpdatedAt(ticket.updatedAt());
    return map(repository.save(entity));
  }

  private SupportTicket map(SupportTicketEntity entity) {
    return new SupportTicket(
        entity.getId(),
        entity.getUserId(),
        entity.getEmail(),
        entity.getCategory(),
        entity.getSubject(),
        entity.getMessage(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
