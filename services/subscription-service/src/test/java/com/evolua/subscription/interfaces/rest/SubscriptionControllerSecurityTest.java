package com.evolua.subscription.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.evolua.subscription.application.AdMobSsvVerifier;
import com.evolua.subscription.application.PlanCatalogService;
import com.evolua.subscription.application.SubscriptionService;
import com.evolua.subscription.config.BillingProperties;
import com.evolua.subscription.infrastructure.security.CurrentUserProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class SubscriptionControllerSecurityTest {
  @Test
  void internalEndpointRejectsInvalidToken() {
    var billingProperties = new BillingProperties();
    billingProperties.setInternalToken("expected-token");
    var controller =
        new SubscriptionController(
            mock(SubscriptionService.class),
            mock(PlanCatalogService.class),
            mock(SubscriptionMapper.class),
            mock(CurrentUserProvider.class),
            billingProperties,
            mock(AdMobSsvVerifier.class));

    var exception =
        assertThrows(ResponseStatusException.class, () -> controller.access("user-1", "wrong-token"));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
  }

  @Test
  void adMobCallbackRejectsInvalidSignatureBeforeGrantingReward() {
    var service = mock(SubscriptionService.class);
    var verifier = mock(AdMobSsvVerifier.class);
    var request = mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn("custom_data=session-1&signature=bad&key_id=123");
    doThrow(new IllegalArgumentException("Invalid AdMob rewarded callback signature"))
        .when(verifier)
        .verify("custom_data=session-1&signature=bad&key_id=123");
    var controller =
        new SubscriptionController(
            service,
            mock(PlanCatalogService.class),
            mock(SubscriptionMapper.class),
            mock(CurrentUserProvider.class),
            new BillingProperties(),
            verifier);

    var exception =
        assertThrows(
            ResponseStatusException.class,
            () -> controller.adMobRewardCallback(request, "session-1", "tx-1"));

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    verifyNoInteractions(service);
  }
}
