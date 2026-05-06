package com.evolua.user.interfaces.rest;

import com.evolua.user.application.FeedbackSubmissionInput;

public record FeedbackSubmissionRequest(
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
    String ratingComment) {
  public FeedbackSubmissionInput toInput() {
    return new FeedbackSubmissionInput(
        workingWell,
        couldImprove,
        confusingOrHard,
        helpedHow,
        featureSuggestion,
        contentSuggestion,
        visualSuggestion,
        aiSuggestion,
        problemWhatHappened,
        problemWhere,
        problemCanRepeat,
        rating,
        ratingComment);
  }
}
