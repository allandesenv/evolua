package com.evolua.subscription.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingEventJpaRepository extends JpaRepository<BillingEventEntity, Long> {
  boolean existsByProviderAndProviderEventId(String provider, String providerEventId);
}
