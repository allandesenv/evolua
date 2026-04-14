package com.evolua.subscription.infrastructure.persistence;

import com.evolua.subscription.domain.BillingEvent;
import com.evolua.subscription.domain.BillingEventRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BillingEventPersistenceAdapter implements BillingEventRepository {
  private final BillingEventJpaRepository repository;

  public BillingEventPersistenceAdapter(BillingEventJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public BillingEvent save(BillingEvent item) {
    var entity = new BillingEventEntity();
    entity.setId(item.id());
    entity.setProvider(item.provider());
    entity.setProviderEventId(item.providerEventId());
    entity.setEventType(item.eventType());
    entity.setCheckoutPublicId(item.checkoutPublicId());
    entity.setPayloadJson(item.payloadJson());
    entity.setCreatedAt(item.createdAt());
    entity = repository.save(entity);
    return new BillingEvent(
        entity.getId(),
        entity.getProvider(),
        entity.getProviderEventId(),
        entity.getEventType(),
        entity.getCheckoutPublicId(),
        entity.getPayloadJson(),
        entity.getCreatedAt());
  }

  @Override
  public boolean existsByProviderAndProviderEventId(String provider, String providerEventId) {
    return repository.existsByProviderAndProviderEventId(provider, providerEventId);
  }
}
