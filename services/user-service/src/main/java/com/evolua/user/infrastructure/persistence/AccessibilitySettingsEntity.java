package com.evolua.user.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_accessibility_preferences")
public class AccessibilitySettingsEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;
  private String themeMode;
  private Boolean highContrast;
  private Boolean reduceTransparency;
  private String animationLevel;
  private String textSize;
  private String readingSpacing;
  private Boolean accessibleFont;
  private Boolean focusMode;
  private Boolean reduceMotion;
  private Boolean hapticFeedback;
  private Boolean extendedResponseTime;
  private Boolean simplifiedNavigation;
  private Boolean reduceVisualStimuli;
  private Boolean softerLanguage;
  private Boolean hideSensitiveContent;
  private Boolean comfortMode;
  private Instant updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getThemeMode() { return themeMode; }
  public void setThemeMode(String themeMode) { this.themeMode = themeMode; }
  public Boolean getHighContrast() { return highContrast; }
  public void setHighContrast(Boolean highContrast) { this.highContrast = highContrast; }
  public Boolean getReduceTransparency() { return reduceTransparency; }
  public void setReduceTransparency(Boolean reduceTransparency) { this.reduceTransparency = reduceTransparency; }
  public String getAnimationLevel() { return animationLevel; }
  public void setAnimationLevel(String animationLevel) { this.animationLevel = animationLevel; }
  public String getTextSize() { return textSize; }
  public void setTextSize(String textSize) { this.textSize = textSize; }
  public String getReadingSpacing() { return readingSpacing; }
  public void setReadingSpacing(String readingSpacing) { this.readingSpacing = readingSpacing; }
  public Boolean getAccessibleFont() { return accessibleFont; }
  public void setAccessibleFont(Boolean accessibleFont) { this.accessibleFont = accessibleFont; }
  public Boolean getFocusMode() { return focusMode; }
  public void setFocusMode(Boolean focusMode) { this.focusMode = focusMode; }
  public Boolean getReduceMotion() { return reduceMotion; }
  public void setReduceMotion(Boolean reduceMotion) { this.reduceMotion = reduceMotion; }
  public Boolean getHapticFeedback() { return hapticFeedback; }
  public void setHapticFeedback(Boolean hapticFeedback) { this.hapticFeedback = hapticFeedback; }
  public Boolean getExtendedResponseTime() { return extendedResponseTime; }
  public void setExtendedResponseTime(Boolean extendedResponseTime) { this.extendedResponseTime = extendedResponseTime; }
  public Boolean getSimplifiedNavigation() { return simplifiedNavigation; }
  public void setSimplifiedNavigation(Boolean simplifiedNavigation) { this.simplifiedNavigation = simplifiedNavigation; }
  public Boolean getReduceVisualStimuli() { return reduceVisualStimuli; }
  public void setReduceVisualStimuli(Boolean reduceVisualStimuli) { this.reduceVisualStimuli = reduceVisualStimuli; }
  public Boolean getSofterLanguage() { return softerLanguage; }
  public void setSofterLanguage(Boolean softerLanguage) { this.softerLanguage = softerLanguage; }
  public Boolean getHideSensitiveContent() { return hideSensitiveContent; }
  public void setHideSensitiveContent(Boolean hideSensitiveContent) { this.hideSensitiveContent = hideSensitiveContent; }
  public Boolean getComfortMode() { return comfortMode; }
  public void setComfortMode(Boolean comfortMode) { this.comfortMode = comfortMode; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
