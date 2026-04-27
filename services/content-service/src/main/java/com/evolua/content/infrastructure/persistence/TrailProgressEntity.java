package com.evolua.content.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "trail_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "trail_id"}))
public class TrailProgressEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "trail_id", nullable = false)
  private Long trailId;

  @Column(name = "current_step_index", nullable = false)
  private Integer currentStepIndex;

  @Column(name = "completed_step_indexes", nullable = false, columnDefinition = "TEXT")
  private String completedStepIndexes;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Long getTrailId() {
    return trailId;
  }

  public void setTrailId(Long trailId) {
    this.trailId = trailId;
  }

  public Integer getCurrentStepIndex() {
    return currentStepIndex;
  }

  public void setCurrentStepIndex(Integer currentStepIndex) {
    this.currentStepIndex = currentStepIndex;
  }

  public String getCompletedStepIndexes() {
    return completedStepIndexes;
  }

  public void setCompletedStepIndexes(String completedStepIndexes) {
    this.completedStepIndexes = completedStepIndexes;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }
}
