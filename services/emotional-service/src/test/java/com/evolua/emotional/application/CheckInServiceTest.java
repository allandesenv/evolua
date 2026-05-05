package com.evolua.emotional.application;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evolua.emotional.domain.CheckInRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class CheckInServiceTest {
  @Test
  void freeUserHistoryIsLimitedToLastThirtyDays() {
    var repository = mock(CheckInRepository.class);
    var subscriptionAccessClient = mock(SubscriptionAccessClient.class);
    when(subscriptionAccessClient.hasPremiumAccess("free-user")).thenReturn(false);
    when(repository.findAllByUserId(eq("free-user"), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());
    var service = new CheckInService(repository, subscriptionAccessClient);
    var oldFrom = Instant.now().minusSeconds(60L * 60 * 24 * 90);

    service.list("free-user", PageRequest.of(0, 10), null, null, null, null, oldFrom, null);

    var fromCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(repository)
        .findAllByUserId(eq("free-user"), any(), any(), any(), any(), any(), fromCaptor.capture(), any());
    assertTrue(fromCaptor.getValue().isAfter(Instant.now().minusSeconds(60L * 60 * 24 * 31)));
  }

  @Test
  void premiumUserKeepsRequestedHistoryRange() {
    var repository = mock(CheckInRepository.class);
    var subscriptionAccessClient = mock(SubscriptionAccessClient.class);
    when(subscriptionAccessClient.hasPremiumAccess("premium-user")).thenReturn(true);
    when(repository.findAllByUserId(eq("premium-user"), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());
    var service = new CheckInService(repository, subscriptionAccessClient);
    var oldFrom = Instant.parse("2025-01-01T00:00:00Z");

    service.list("premium-user", PageRequest.of(0, 10), null, null, null, null, oldFrom, null);

    verify(repository)
        .findAllByUserId(eq("premium-user"), any(), any(), any(), any(), any(), eq(oldFrom), any());
  }
}
