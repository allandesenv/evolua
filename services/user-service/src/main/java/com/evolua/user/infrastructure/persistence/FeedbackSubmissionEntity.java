package com.evolua.user.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "feedback_submissions")
public class FeedbackSubmissionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userId;
  private String email;
  private String workingWell;
  private String couldImprove;
  private String confusingOrHard;
  private String helpedHow;
  private String featureSuggestion;
  private String contentSuggestion;
  private String visualSuggestion;
  private String aiSuggestion;
  private String problemWhatHappened;
  private String problemWhere;
  private String problemCanRepeat;
  private String rating;
  private String ratingComment;
  private String screenshotFileName;
  private String status;
  private Instant createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getWorkingWell() { return workingWell; }
  public void setWorkingWell(String workingWell) { this.workingWell = workingWell; }
  public String getCouldImprove() { return couldImprove; }
  public void setCouldImprove(String couldImprove) { this.couldImprove = couldImprove; }
  public String getConfusingOrHard() { return confusingOrHard; }
  public void setConfusingOrHard(String confusingOrHard) { this.confusingOrHard = confusingOrHard; }
  public String getHelpedHow() { return helpedHow; }
  public void setHelpedHow(String helpedHow) { this.helpedHow = helpedHow; }
  public String getFeatureSuggestion() { return featureSuggestion; }
  public void setFeatureSuggestion(String featureSuggestion) { this.featureSuggestion = featureSuggestion; }
  public String getContentSuggestion() { return contentSuggestion; }
  public void setContentSuggestion(String contentSuggestion) { this.contentSuggestion = contentSuggestion; }
  public String getVisualSuggestion() { return visualSuggestion; }
  public void setVisualSuggestion(String visualSuggestion) { this.visualSuggestion = visualSuggestion; }
  public String getAiSuggestion() { return aiSuggestion; }
  public void setAiSuggestion(String aiSuggestion) { this.aiSuggestion = aiSuggestion; }
  public String getProblemWhatHappened() { return problemWhatHappened; }
  public void setProblemWhatHappened(String problemWhatHappened) { this.problemWhatHappened = problemWhatHappened; }
  public String getProblemWhere() { return problemWhere; }
  public void setProblemWhere(String problemWhere) { this.problemWhere = problemWhere; }
  public String getProblemCanRepeat() { return problemCanRepeat; }
  public void setProblemCanRepeat(String problemCanRepeat) { this.problemCanRepeat = problemCanRepeat; }
  public String getRating() { return rating; }
  public void setRating(String rating) { this.rating = rating; }
  public String getRatingComment() { return ratingComment; }
  public void setRatingComment(String ratingComment) { this.ratingComment = ratingComment; }
  public String getScreenshotFileName() { return screenshotFileName; }
  public void setScreenshotFileName(String screenshotFileName) { this.screenshotFileName = screenshotFileName; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
