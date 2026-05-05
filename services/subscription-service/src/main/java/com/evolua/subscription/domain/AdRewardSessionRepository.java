package com.evolua.subscription.domain;

public interface AdRewardSessionRepository {
  AdRewardSession save(AdRewardSession item);

  AdRewardSession findByPublicId(String publicId);

  boolean existsGrantedByUserIdAndRewardTypeAndProviderTransactionId(
      String userId, String rewardType, String providerTransactionId);
}
