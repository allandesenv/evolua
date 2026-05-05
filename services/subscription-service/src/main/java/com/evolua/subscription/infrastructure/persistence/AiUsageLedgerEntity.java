package com.evolua.subscription.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "ai_usage_ledger",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_ai_usage_user_resource_date",
            columnNames = {"userId", "resource", "usageDate"}))
public class AiUsageLedgerEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String resource;

  @Column(nullable = false)
  private LocalDate usageDate;

  @Column(nullable = false)
  private Integer baseUsed;

  @Column(nullable = false)
  private Integer rewardUsed;

  @Column(nullable = false)
  private Integer rewardGranted;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getResource() { return resource; }
  public void setResource(String resource) { this.resource = resource; }
  public LocalDate getUsageDate() { return usageDate; }
  public void setUsageDate(LocalDate usageDate) { this.usageDate = usageDate; }
  public Integer getBaseUsed() { return baseUsed; }
  public void setBaseUsed(Integer baseUsed) { this.baseUsed = baseUsed; }
  public Integer getRewardUsed() { return rewardUsed; }
  public void setRewardUsed(Integer rewardUsed) { this.rewardUsed = rewardUsed; }
  public Integer getRewardGranted() { return rewardGranted; }
  public void setRewardGranted(Integer rewardGranted) { this.rewardGranted = rewardGranted; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
