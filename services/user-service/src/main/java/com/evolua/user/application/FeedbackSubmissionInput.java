package com.evolua.user.application;

public record FeedbackSubmissionInput(
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
    String ratingComment) {}
