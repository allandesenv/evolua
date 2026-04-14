package com.evolua.subscription.domain;

public interface BillingEventRepository {
  BillingEvent save(BillingEvent item);

  boolean existsByProviderAndProviderEventId(String provider, String providerEventId);
}
