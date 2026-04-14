package com.evolua.subscription.domain;

import java.math.BigDecimal;
import java.util.List;

public record PlanCatalogItem(
    String planCode,
    String title,
    String subtitle,
    String billingCycle,
    Boolean premium,
    BigDecimal price,
    String currency,
    List<String> benefits,
    Boolean active) {}
