package com.evolua.subscription.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardEntitlementJpaRepository extends JpaRepository<RewardEntitlementEntity, Long> {
  Optional<RewardEntitlementEntity>
      findFirstByUserIdAndEntitlementTypeAndStatusAndStartsAtLessThanEqualAndExpiresAtAfterOrderByExpiresAtDesc(
          String userId, String entitlementType, String status, Instant startsAt, Instant expiresAt);

  boolean existsByUserIdAndEntitlementTypeAndStartsAtGreaterThanEqualAndStartsAtLessThan(
      String userId, String entitlementType, Instant startsAtInclusive, Instant startsAtExclusive);
}
