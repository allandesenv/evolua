package com.evolua.subscription.application;

import com.evolua.subscription.domain.Subscription;
import com.evolua.subscription.domain.SubscriptionRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {
  private final SubscriptionRepository repository;

  public SubscriptionService(SubscriptionRepository repository) {
    this.repository = repository;
  }

  public Subscription create(String userId, String planCode, String status, String billingCycle, Boolean premium) {
    return repository.save(new Subscription(null, userId, planCode, status, billingCycle, premium, Instant.now()));
  }

  public Page<Subscription> list(
      String userId, Pageable pageable, String search, String status, Boolean premium) {
    return repository.findAllByUserId(userId, pageable, search, status, premium);
  }
}
