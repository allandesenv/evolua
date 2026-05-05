package com.evolua.user.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_privacy_preferences")
public class PrivacySettingsEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;
  private Boolean privateJournal;
  private Boolean hideSocialCheckIns;
  private Boolean allowHistoryInsights;
  private Boolean useEmotionalDataForAi;
  private Boolean dailyReminders;
  private Boolean contentPreferences;
  private String aiTone;
  private String suggestionFrequency;
  private String trailStyle;
  private Instant updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public Boolean getPrivateJournal() { return privateJournal; }
  public void setPrivateJournal(Boolean privateJournal) { this.privateJournal = privateJournal; }
  public Boolean getHideSocialCheckIns() { return hideSocialCheckIns; }
  public void setHideSocialCheckIns(Boolean hideSocialCheckIns) { this.hideSocialCheckIns = hideSocialCheckIns; }
  public Boolean getAllowHistoryInsights() { return allowHistoryInsights; }
  public void setAllowHistoryInsights(Boolean allowHistoryInsights) { this.allowHistoryInsights = allowHistoryInsights; }
  public Boolean getUseEmotionalDataForAi() { return useEmotionalDataForAi; }
  public void setUseEmotionalDataForAi(Boolean useEmotionalDataForAi) { this.useEmotionalDataForAi = useEmotionalDataForAi; }
  public Boolean getDailyReminders() { return dailyReminders; }
  public void setDailyReminders(Boolean dailyReminders) { this.dailyReminders = dailyReminders; }
  public Boolean getContentPreferences() { return contentPreferences; }
  public void setContentPreferences(Boolean contentPreferences) { this.contentPreferences = contentPreferences; }
  public String getAiTone() { return aiTone; }
  public void setAiTone(String aiTone) { this.aiTone = aiTone; }
  public String getSuggestionFrequency() { return suggestionFrequency; }
  public void setSuggestionFrequency(String suggestionFrequency) { this.suggestionFrequency = suggestionFrequency; }
  public String getTrailStyle() { return trailStyle; }
  public void setTrailStyle(String trailStyle) { this.trailStyle = trailStyle; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
