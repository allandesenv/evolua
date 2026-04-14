package com.evolua.subscription.domain;

public interface BillingCheckoutRepository {
  BillingCheckout save(BillingCheckout item);

  BillingCheckout findByPublicId(String publicId);
}
