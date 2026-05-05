package com.evolua.emotional.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "check_ins")
public class CheckInEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;
  private String mood;
  private String reflection;
  private Integer energyLevel;
  private String recommendedPractice;
  private Instant createdAt;
  private String emotion;
  private Integer intensity;
  private String energy;
  private String context;
  private String decisionTags;
  private String severityLevel;

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

  public String getMood() {
    return mood;
  }

  public void setMood(String mood) {
    this.mood = mood;
  }

  public String getReflection() {
    return reflection;
  }

  public void setReflection(String reflection) {
    this.reflection = reflection;
  }

  public Integer getEnergyLevel() {
    return energyLevel;
  }

  public void setEnergyLevel(Integer energyLevel) {
    this.energyLevel = energyLevel;
  }

  public String getRecommendedPractice() {
    return recommendedPractice;
  }

  public void setRecommendedPractice(String recommendedPractice) {
    this.recommendedPractice = recommendedPractice;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public String getEmotion() {
    return emotion;
  }

  public void setEmotion(String emotion) {
    this.emotion = emotion;
  }

  public Integer getIntensity() {
    return intensity;
  }

  public void setIntensity(Integer intensity) {
    this.intensity = intensity;
  }

  public String getEnergy() {
    return energy;
  }

  public void setEnergy(String energy) {
    this.energy = energy;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getDecisionTags() {
    return decisionTags;
  }

  public void setDecisionTags(String decisionTags) {
    this.decisionTags = decisionTags;
  }

  public String getSeverityLevel() {
    return severityLevel;
  }

  public void setSeverityLevel(String severityLevel) {
    this.severityLevel = severityLevel;
  }
}
