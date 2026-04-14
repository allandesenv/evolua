package com.evolua.subscription.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "billing_events",
    uniqueConstraints = @UniqueConstraint(name = "uq_billing_events_provider_event", columnNames = {"provider", "providerEventId"}))
public class BillingEventEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String provider;
  private String providerEventId;
  private String eventType;
  private String checkoutPublicId;
  @Column(columnDefinition = "TEXT")
  private String payloadJson;
  private Instant createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }
  public String getProviderEventId() { return providerEventId; }
  public void setProviderEventId(String providerEventId) { this.providerEventId = providerEventId; }
  public String getEventType() { return eventType; }
  public void setEventType(String eventType) { this.eventType = eventType; }
  public String getCheckoutPublicId() { return checkoutPublicId; }
  public void setCheckoutPublicId(String checkoutPublicId) { this.checkoutPublicId = checkoutPublicId; }
  public String getPayloadJson() { return payloadJson; }
  public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
