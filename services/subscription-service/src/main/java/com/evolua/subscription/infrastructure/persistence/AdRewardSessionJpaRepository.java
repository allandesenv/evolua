package com.evolua.subscription.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdRewardSessionJpaRepository extends JpaRepository<AdRewardSessionEntity, Long> {
  Optional<AdRewardSessionEntity> findByPublicId(String publicId);

  boolean existsByUserIdAndRewardTypeAndStatusAndProviderTransactionId(
      String userId, String rewardType, String status, String providerTransactionId);
}
