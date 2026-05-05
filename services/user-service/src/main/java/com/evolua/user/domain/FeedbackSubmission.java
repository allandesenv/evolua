package com.evolua.user.domain;

import java.time.Instant;

public record FeedbackSubmission(
    Long id,
    String userId,
    String email,
    String workingWell,
    String couldImprove,
    String confusingOrHard,
    String helpedHow,
    String featureSuggestion,
    String contentSuggestion,
    String visualSuggestion,
    String aiSuggestion,
    String problemWhatHappened,
    String problemWhere,
    String problemCanRepeat,
    String rating,
    String ratingComment,
    String screenshotFileName,
    String status,
    Instant createdAt) {}
