package com.evolua.subscription.interfaces.rest;

import java.math.BigDecimal;
import java.util.List;

public record PlanViewResponse(
    String planCode,
    String title,
    String subtitle,
    String billingCycle,
    Boolean premium,
    BigDecimal price,
    String currency,
    List<String> benefits,
    Boolean active) {}
