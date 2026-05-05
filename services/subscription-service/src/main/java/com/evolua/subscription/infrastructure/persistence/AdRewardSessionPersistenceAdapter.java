package com.evolua.subscription.infrastructure.persistence;

import com.evolua.subscription.domain.AdRewardSession;
import com.evolua.subscription.domain.AdRewardSessionRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AdRewardSessionPersistenceAdapter implements AdRewardSessionRepository {
  private final AdRewardSessionJpaRepository repository;

  public AdRewardSessionPersistenceAdapter(AdRewardSessionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public AdRewardSession save(AdRewardSession item) {
    var entity =
        item.id() == null
            ? new AdRewardSessionEntity()
            : repository.findById(item.id()).orElseGet(AdRewardSessionEntity::new);
    entity.setId(item.id());
    entity.setPublicId(item.publicId());
    entity.setUserId(item.userId());
    entity.setProvider(item.provider());
    entity.setRewardType(item.rewardType());
    entity.setStatus(item.status());
    entity.setProviderTransactionId(item.providerTransactionId());
    entity.setExpiresAt(item.expiresAt());
    entity.setGrantedAt(item.grantedAt());
    entity.setCreatedAt(item.createdAt());
    entity.setUpdatedAt(item.updatedAt());
    return toDomain(repository.save(entity));
  }

  @Override
  public AdRewardSession findByPublicId(String publicId) {
    return repository.findByPublicId(publicId).map(this::toDomain).orElse(null);
  }

  @Override
  public boolean existsGrantedByUserIdAndRewardTypeAndProviderTransactionId(
      String userId, String rewardType, String providerTransactionId) {
    return repository.existsByUserIdAndRewardTypeAndStatusAndProviderTransactionId(
        userId, rewardType, "GRANTED", providerTransactionId);
  }

  private AdRewardSession toDomain(AdRewardSessionEntity entity) {
    return new AdRewardSession(
        entity.getId(),
        entity.getPublicId(),
        entity.getUserId(),
        entity.getProvider(),
        entity.getRewardType(),
        entity.getStatus(),
        entity.getProviderTransactionId(),
        entity.getExpiresAt(),
        entity.getGrantedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
