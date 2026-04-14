package com.evolua.subscription.interfaces.rest;

import com.evolua.subscription.domain.BillingCheckout;
import com.evolua.subscription.domain.PlanCatalogItem;
import com.evolua.subscription.domain.Subscription;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {
  public PlanViewResponse toPlanResponse(PlanCatalogItem item) {
    return new PlanViewResponse(
        item.planCode(),
        item.title(),
        item.subtitle(),
        item.billingCycle(),
        item.premium(),
        item.price(),
        item.currency(),
        item.benefits(),
        item.active());
  }

  public CurrentSubscriptionResponse toCurrentResponse(Subscription item) {
    if (item == null) {
      return null;
    }
    return new CurrentSubscriptionResponse(
        item.id(),
        item.userId(),
        item.planCode(),
        item.status(),
        item.billingCycle(),
        item.premium(),
        item.provider(),
        item.currentPeriodEndsAt(),
        item.canceledAt(),
        item.createdAt(),
        item.updatedAt());
  }

  public BillingCheckoutResponse toCheckoutResponse(BillingCheckout item) {
    if (item == null) {
      return null;
    }
    return new BillingCheckoutResponse(
        item.publicId(),
        item.planCode(),
        item.billingCycle(),
        item.status(),
        item.premium(),
        item.checkoutUrl(),
        item.failureReason(),
        item.createdAt(),
        item.updatedAt(),
        item.confirmedAt());
  }

  public SubscriptionAccessResponse toAccessResponse(Map<String, Object> item) {
    return new SubscriptionAccessResponse(
        String.valueOf(item.get("userId")),
        Boolean.TRUE.equals(item.get("premium")),
        String.valueOf(item.get("status")),
        String.valueOf(item.get("planCode")));
  }
}
