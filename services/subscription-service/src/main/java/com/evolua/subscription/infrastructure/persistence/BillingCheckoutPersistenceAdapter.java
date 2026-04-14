package com.evolua.subscription.infrastructure.persistence;

import com.evolua.subscription.domain.BillingCheckout;
import com.evolua.subscription.domain.BillingCheckoutRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BillingCheckoutPersistenceAdapter implements BillingCheckoutRepository {
  private final BillingCheckoutJpaRepository repository;

  public BillingCheckoutPersistenceAdapter(BillingCheckoutJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public BillingCheckout save(BillingCheckout item) {
    BillingCheckoutEntity entity =
        item.id() == null
            ? new BillingCheckoutEntity()
            : repository.findById(item.id()).orElseGet(BillingCheckoutEntity::new);
    entity.setId(item.id());
    entity.setPublicId(item.publicId());
    entity.setUserId(item.userId());
    entity.setPlanCode(item.planCode());
    entity.setBillingCycle(item.billingCycle());
    entity.setProvider(item.provider());
    entity.setStatus(item.status());
    entity.setPremium(item.premium());
    entity.setProviderPreferenceId(item.providerPreferenceId());
    entity.setProviderPaymentId(item.providerPaymentId());
    entity.setCheckoutUrl(item.checkoutUrl());
    entity.setFailureReason(item.failureReason());
    entity.setCreatedAt(item.createdAt());
    entity.setUpdatedAt(item.updatedAt());
    entity.setConfirmedAt(item.confirmedAt());
    return toDomain(repository.save(entity));
  }

  @Override
  public BillingCheckout findByPublicId(String publicId) {
    return repository.findByPublicId(publicId).map(this::toDomain).orElse(null);
  }

  private BillingCheckout toDomain(BillingCheckoutEntity item) {
    return new BillingCheckout(
        item.getId(),
        item.getPublicId(),
        item.getUserId(),
        item.getPlanCode(),
        item.getBillingCycle(),
        item.getProvider(),
        item.getStatus(),
        item.getPremium(),
        item.getProviderPreferenceId(),
        item.getProviderPaymentId(),
        item.getCheckoutUrl(),
        item.getFailureReason(),
        item.getCreatedAt(),
        item.getUpdatedAt(),
        item.getConfirmedAt());
  }
}
