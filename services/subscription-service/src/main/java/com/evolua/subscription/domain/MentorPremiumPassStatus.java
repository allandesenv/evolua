package com.evolua.subscription.domain;

import java.time.Instant;

public record MentorPremiumPassStatus(Boolean active, Instant endsAt, Boolean rewardedAdAvailable) {}
