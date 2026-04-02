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

  public Subscription save(Subscription item) {
    SubscriptionEntity entity = new SubscriptionEntity();
    entity.setId(item.id());
    entity.setUserId(item.userId());
    entity.setPlanCode(item.planCode());
    entity.setStatus(item.status());
    entity.setBillingCycle(item.billingCycle());
    entity.setPremium(item.premium());
    entity.setCreatedAt(item.createdAt());
    SubscriptionEntity saved = repository.save(entity);
    return new Subscription(
        saved.getId(),
        saved.getUserId(),
        saved.getPlanCode(),
        saved.getStatus(),
        saved.getBillingCycle(),
        saved.getPremium(),
        saved.getCreatedAt());
  }

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
          specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("status")), normalizedStatus));
    }

    if (premium != null) {
      specification = specification.and((root, query, cb) -> cb.equal(root.get("premium"), premium));
    }

    return repository.findAll(specification, pageable)
        .map(
            saved ->
                new Subscription(
                    saved.getId(),
                    saved.getUserId(),
                    saved.getPlanCode(),
                    saved.getStatus(),
                    saved.getBillingCycle(),
                    saved.getPremium(),
                    saved.getCreatedAt()));
  }
}
