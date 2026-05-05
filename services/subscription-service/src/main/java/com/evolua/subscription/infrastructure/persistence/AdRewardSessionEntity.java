package com.evolua.subscription.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ad_reward_sessions")
public class AdRewardSessionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String publicId;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String provider;

  @Column(nullable = false)
  private String rewardType;

  @Column(nullable = false)
  private String status;

  private String providerTransactionId;

  @Column(nullable = false)
  private Instant expiresAt;

  private Instant grantedAt;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getPublicId() { return publicId; }
  public void setPublicId(String publicId) { this.publicId = publicId; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }
  public String getRewardType() { return rewardType; }
  public void setRewardType(String rewardType) { this.rewardType = rewardType; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getProviderTransactionId() { return providerTransactionId; }
  public void setProviderTransactionId(String providerTransactionId) { this.providerTransactionId = providerTransactionId; }
  public Instant getExpiresAt() { return expiresAt; }
  public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
  public Instant getGrantedAt() { return grantedAt; }
  public void setGrantedAt(Instant grantedAt) { this.grantedAt = grantedAt; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
