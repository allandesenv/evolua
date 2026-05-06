package com.evolua.content.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.evolua.content.application.SubscriptionAccessClient;
import com.evolua.content.application.TrailJourneyService;
import com.evolua.content.application.TrailService;
import com.evolua.content.domain.Trail;
import com.evolua.content.infrastructure.security.AuthenticatedUser;
import com.evolua.content.infrastructure.security.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class TrailControllerAccessTest {
  @Test
  void mentorPassUnlocksOnlyMentorPremiumTrails() {
    var service = mock(TrailService.class);
    var currentUserProvider = mock(CurrentUserProvider.class);
    var accessClient = mock(SubscriptionAccessClient.class);
    var controller =
        new TrailController(
            service, mock(TrailJourneyService.class), currentUserProvider, accessClient);
    var mentorTrail = premiumTrail(1L, "Mentoria guiada", "mentoria", "mentor_exclusive");
    var regularPremiumTrail = premiumTrail(2L, "Sono premium", "sono", null);

    when(currentUserProvider.getCurrentUser())
        .thenReturn(new AuthenticatedUser("user-1", "user@evolua.local", List.of("ROLE_USER")));
    when(accessClient.accessSummary("user-1"))
        .thenReturn(new SubscriptionAccessClient.AccessSummary(false, true));
    when(service.list(eq("user-1"), any(Pageable.class), eq(""), eq(null), eq(null)))
        .thenReturn(new PageImpl<>(List.of(mentorTrail, regularPremiumTrail)));

    var response = controller.list(0, 20, null, null, null, null, null).getBody();
    var items = response.data().items();

    assertTrue(items.get(0).accessible());
    assertFalse(items.get(1).accessible());
  }

  private Trail premiumTrail(Long id, String title, String category, String sourceStyle) {
    return new Trail(
        id,
        "admin",
        title,
        "Resumo",
        "Conteudo completo",
        category,
        true,
        false,
        false,
        false,
        null,
        sourceStyle,
        List.of(),
        Instant.parse("2026-05-06T12:00:00Z"));
  }
}
