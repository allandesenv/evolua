package com.evolua.subscription.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiUsageLedgerJpaRepository extends JpaRepository<AiUsageLedgerEntity, Long> {
  Optional<AiUsageLedgerEntity> findByUserIdAndResourceAndUsageDate(
      String userId, String resource, LocalDate usageDate);
}
