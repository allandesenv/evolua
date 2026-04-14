package com.evolua.subscription.infrastructure.persistence;

import com.evolua.subscription.domain.Subscription;
import com.evolua.subscription.domain.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionPersistenceAdapter implements SubscriptionRepository {
  private final SubscriptionJpaRepository repository;

  public SubscriptionPersistenceAdapter(SubscriptionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Subscription save(Subscription item) {
    SubscriptionEntity entity =
        item.id() == null
            ? new SubscriptionEntity()
            : repository.findById(item.id()).orElseGet(SubscriptionEntity::new);
    entity.setId(item.id());
    entity.setUserId(item.userId());
    entity.setPlanCode(item.planCode());
    entity.setStatus(item.status());
    entity.setBillingCycle(item.billingCycle());
    entity.setPremium(item.premium());
    entity.setProvider(item.provider());
    entity.setProviderCustomerId(item.providerCustomerId());
    entity.setProviderPaymentId(item.providerPaymentId());
    entity.setProviderSubscriptionId(item.providerSubscriptionId());
    entity.setCurrentPeriodEndsAt(item.currentPeriodEndsAt());
    entity.setCanceledAt(item.canceledAt());
    entity.setCreatedAt(item.createdAt());
    entity.setUpdatedAt(item.updatedAt());
    return toDomain(repository.save(entity));
  }

  @Override
  public Page<Subscription> findAllByUserId(
      String userId, Pageable pageable, String search, String status, Boolean premium) {
    Specification<SubscriptionEntity> specification =
        Specification.where((root, query, cb) -> cb.equal(root.get("userId"), userId));

    if (search != null && !search.isBlank()) {
      var normalized = "%" + search.toLowerCase() + "%";
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("planCode")), normalized),
                      cb.like(cb.lower(root.get("status")), normalized),
                      cb.like(cb.lower(root.get("billingCycle")), normalized)));
    }

    if (status != null && !status.isBlank()) {
      var normalizedStatus = status.toLowerCase();
      specification =
          specification.and(
              (root, query, cb) -> cb.equal(cb.lower(root.get("status")), normalizedStatus));
    }

    if (premium != null) {
      specification = specification.and((root, query, cb) -> cb.equal(root.get("premium"), premium));
    }

    return repository.findAll(specification, pageable).map(this::toDomain);
  }

  @Override
  public Subscription findCurrentByUserId(String userId) {
    return repository.findFirstByUserIdOrderByUpdatedAtDescCreatedAtDesc(userId).map(this::toDomain).orElse(null);
  }

  private Subscription toDomain(SubscriptionEntity saved) {
    return new Subscription(
        saved.getId(),
        saved.getUserId(),
        saved.getPlanCode(),
        saved.getStatus(),
        saved.getBillingCycle(),
        saved.getPremium(),
        saved.getProvider(),
        saved.getProviderCustomerId(),
        saved.getProviderPaymentId(),
        saved.getProviderSubscriptionId(),
        saved.getCurrentPeriodEndsAt(),
        saved.getCanceledAt(),
        saved.getCreatedAt(),
        saved.getUpdatedAt());
  }
}
