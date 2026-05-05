package com.evolua.subscription.infrastructure.persistence;

import com.evolua.subscription.domain.AiUsageLedger;
import com.evolua.subscription.domain.AiUsageLedgerRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Repository;

@Repository
public class AiUsageLedgerPersistenceAdapter implements AiUsageLedgerRepository {
  private final AiUsageLedgerJpaRepository repository;

  public AiUsageLedgerPersistenceAdapter(AiUsageLedgerJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public AiUsageLedger save(AiUsageLedger item) {
    var entity =
        item.id() == null
            ? new AiUsageLedgerEntity()
            : repository.findById(item.id()).orElseGet(AiUsageLedgerEntity::new);
    entity.setId(item.id());
    entity.setUserId(item.userId());
    entity.setResource(item.resource());
    entity.setUsageDate(item.usageDate());
    entity.setBaseUsed(item.baseUsed());
    entity.setRewardUsed(item.rewardUsed());
    entity.setRewardGranted(item.rewardGranted());
    entity.setCreatedAt(item.createdAt());
    entity.setUpdatedAt(item.updatedAt());
    return toDomain(repository.save(entity));
  }

  @Override
  public AiUsageLedger findByUserIdAndResourceAndUsageDate(
      String userId, String resource, LocalDate usageDate) {
    return repository
        .findByUserIdAndResourceAndUsageDate(userId, resource, usageDate)
        .map(this::toDomain)
        .orElse(null);
  }

  private AiUsageLedger toDomain(AiUsageLedgerEntity entity) {
    return new AiUsageLedger(
        entity.getId(),
        entity.getUserId(),
        entity.getResource(),
        entity.getUsageDate(),
        entity.getBaseUsed(),
        entity.getRewardUsed(),
        entity.getRewardGranted(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
