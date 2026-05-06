package com.evolua.subscription.infrastructure.persistence;

import com.evolua.subscription.domain.RewardEntitlement;
import com.evolua.subscription.domain.RewardEntitlementRepository;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class RewardEntitlementPersistenceAdapter implements RewardEntitlementRepository {
  private final RewardEntitlementJpaRepository repository;

  public RewardEntitlementPersistenceAdapter(RewardEntitlementJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public RewardEntitlement save(RewardEntitlement item) {
    var entity =
        item.id() == null
            ? new RewardEntitlementEntity()
            : repository.findById(item.id()).orElseGet(RewardEntitlementEntity::new);
    entity.setId(item.id());
    entity.setUserId(item.userId());
    entity.setEntitlementType(item.entitlementType());
    entity.setSourceRewardSessionId(item.sourceRewardSessionId());
    entity.setStatus(item.status());
    entity.setStartsAt(item.startsAt());
    entity.setExpiresAt(item.expiresAt());
    entity.setCreatedAt(item.createdAt());
    entity.setUpdatedAt(item.updatedAt());
    return toDomain(repository.save(entity));
  }

  @Override
  public RewardEntitlement findActive(String userId, String entitlementType, Instant now) {
    return repository
        .findFirstByUserIdAndEntitlementTypeAndStatusAndStartsAtLessThanEqualAndExpiresAtAfterOrderByExpiresAtDesc(
            userId, entitlementType, "ACTIVE", now, now)
        .map(this::toDomain)
        .orElse(null);
  }

  @Override
  public boolean existsStartedBetween(
      String userId, String entitlementType, Instant startsAtInclusive, Instant startsAtExclusive) {
    return repository.existsByUserIdAndEntitlementTypeAndStartsAtGreaterThanEqualAndStartsAtLessThan(
        userId, entitlementType, startsAtInclusive, startsAtExclusive);
  }

  private RewardEntitlement toDomain(RewardEntitlementEntity entity) {
    return new RewardEntitlement(
        entity.getId(),
        entity.getUserId(),
        entity.getEntitlementType(),
        entity.getSourceRewardSessionId(),
        entity.getStatus(),
        entity.getStartsAt(),
        entity.getExpiresAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
