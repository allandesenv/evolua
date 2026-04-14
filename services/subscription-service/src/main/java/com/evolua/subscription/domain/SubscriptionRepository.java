package com.evolua.subscription.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionRepository {
  Subscription save(Subscription item);

  Page<Subscription> findAllByUserId(
      String userId, Pageable pageable, String search, String status, Boolean premium);

  Subscription findCurrentByUserId(String userId);
}
