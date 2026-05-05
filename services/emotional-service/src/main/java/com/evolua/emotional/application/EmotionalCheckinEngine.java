package com.evolua.emotional.application;

import com.evolua.emotional.domain.CheckIn;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EmotionalCheckinEngine {
  private final CheckinRuleEvaluator ruleEvaluator;

  public EmotionalCheckinEngine(CheckinRuleEvaluator ruleEvaluator) {
    this.ruleEvaluator = ruleEvaluator;
  }

  public CheckInDecision decide(CheckInDecisionInput input, List<CheckIn> recentHistory) {
    return ruleEvaluator.evaluate(input, recentHistory == null ? List.of() : recentHistory);
  }
}
