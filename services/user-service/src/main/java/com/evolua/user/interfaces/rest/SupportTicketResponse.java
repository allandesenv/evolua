package com.evolua.user.interfaces.rest;

import com.evolua.user.domain.SupportTicket;
import java.time.Instant;

public record SupportTicketResponse(Long id, String category, String status, Instant createdAt) {
  public static SupportTicketResponse from(SupportTicket ticket) {
    return new SupportTicketResponse(
        ticket.id(), ticket.category(), ticket.status(), ticket.createdAt());
  }
}
