package com.evolua.subscription.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingCheckoutJpaRepository extends JpaRepository<BillingCheckoutEntity, Long> {
  Optional<BillingCheckoutEntity> findByPublicId(String publicId);
}
