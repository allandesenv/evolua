package com.evolua.subscription.interfaces.rest;

import com.evolua.subscription.domain.BillingCheckout;
import com.evolua.subscription.domain.AiQuotaStatus;
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
        !Boolean.TRUE.equals(item.premium()),
        null,
        item.currentPeriodEndsAt(),
        item.canceledAt(),
        item.createdAt(),
        item.updatedAt());
  }

  public CurrentSubscriptionResponse toCurrentResponse(Subscription item, AiQuotaStatus quota) {
    if (item == null) {
      return new CurrentSubscriptionResponse(
          null,
          quota.userId(),
          quota.planCode(),
          "NONE",
          "MONTHLY",
          quota.premium(),
          "MANUAL",
          !Boolean.TRUE.equals(quota.premium()),
          quota.remainingToday(),
          null,
          null,
          null,
          null);
    }
    return new CurrentSubscriptionResponse(
        item.id(),
        item.userId(),
        item.planCode(),
        item.status(),
        item.billingCycle(),
        item.premium(),
        item.provider(),
        !Boolean.TRUE.equals(item.premium()),
        quota.remainingToday(),
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
        String.valueOf(item.get("planCode")),
        Boolean.TRUE.equals(item.get("adsEnabled")),
        item.get("aiQuotaRemainingToday") instanceof Number number ? number.intValue() : 0);
  }
}
