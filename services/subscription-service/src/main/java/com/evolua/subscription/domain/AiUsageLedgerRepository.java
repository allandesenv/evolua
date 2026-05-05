package com.evolua.subscription.domain;

import java.time.LocalDate;

public interface AiUsageLedgerRepository {
  AiUsageLedger save(AiUsageLedger item);

  AiUsageLedger findByUserIdAndResourceAndUsageDate(String userId, String resource, LocalDate usageDate);
}
