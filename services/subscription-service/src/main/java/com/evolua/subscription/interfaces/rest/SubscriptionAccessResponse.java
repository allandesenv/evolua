package com.evolua.subscription.interfaces.rest;

public record SubscriptionAccessResponse(
    String userId, Boolean premium, String status, String planCode) {}
