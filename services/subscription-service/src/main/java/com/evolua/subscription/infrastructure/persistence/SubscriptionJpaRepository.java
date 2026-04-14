package com.evolua.subscription.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubscriptionJpaRepository
    extends JpaRepository<SubscriptionEntity, Long>, JpaSpecificationExecutor<SubscriptionEntity> {
  Optional<SubscriptionEntity> findFirstByUserIdOrderByUpdatedAtDescCreatedAtDesc(String userId);
}
