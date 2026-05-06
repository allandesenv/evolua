package com.evolua.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.evolua.user.domain.SupportTicket;
import com.evolua.user.domain.SupportTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SupportServiceTest {
  private SupportTicketRepository repository;
  private SupportStatusHealthClient healthClient;
  private SupportService service;

  @BeforeEach
  void setUp() {
    repository = mock(SupportTicketRepository.class);
    healthClient = mock(SupportStatusHealthClient.class);
    service =
        new SupportService(
            repository,
            healthClient,
            "https://help.evolua.test",
            "https://support.evolua.test",
            "https://care.evolua.test",
            "https://resources.evolua.test",
            "https://limits.evolua.test",
            "https://system.evolua.test/health",
            "https://ai.evolua.test/health",
            "https://notifications.evolua.test/health",
            "https://sync.evolua.test/health");
  }

  @Test
  void createTicketShouldPersistAuthenticatedUserData() {
    when(repository.save(any(SupportTicket.class)))
        .thenAnswer(
            invocation -> {
              var ticket = invocation.getArgument(0, SupportTicket.class);
              return new SupportTicket(
                  10L,
                  ticket.userId(),
                  ticket.email(),
                  ticket.category(),
                  ticket.subject(),
                  ticket.message(),
                  ticket.status(),
                  ticket.createdAt(),
                  ticket.updatedAt());
            });

    var created =
        service.createTicket(
            "user-1", "leo@evolua.local", "technical", "Erro no app", "A tela nao carregou.");

    assertThat(created.id()).isEqualTo(10L);
    assertThat(created.userId()).isEqualTo("user-1");
    assertThat(created.email()).isEqualTo("leo@evolua.local");
    assertThat(created.category()).isEqualTo("TECHNICAL");
    assertThat(created.status()).isEqualTo("OPEN");

    var captor = ArgumentCaptor.forClass(SupportTicket.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().userId()).isEqualTo("user-1");
  }

  @Test
  void createTicketShouldRejectInvalidCategoryAndBlankMessage() {
    assertThatThrownBy(
            () -> service.createTicket("user-1", "leo@evolua.local", "OTHER", "Assunto", "Mensagem"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Categoria de suporte invalida.");

    assertThatThrownBy(
            () -> service.createTicket("user-1", "leo@evolua.local", "GENERAL", "Assunto", " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Conte rapidamente como podemos ajudar.");
  }

  @Test
  void configShouldExposeConfiguredLinks() {
    var config = service.config();

    assertThat(config.helpCenterUrl()).isEqualTo("https://help.evolua.test");
    assertThat(config.supportUrl()).isEqualTo("https://support.evolua.test");
    assertThat(config.professionalHelpUrl()).isEqualTo("https://care.evolua.test");
    assertThat(config.emotionalResourcesUrl()).isEqualTo("https://resources.evolua.test");
    assertThat(config.aiLimitsUrl()).isEqualTo("https://limits.evolua.test");
  }

  @Test
  void statusShouldReturnOperationalAndUnknownItems() {
    when(healthClient.isHealthy("https://system.evolua.test/health")).thenReturn(true);
    when(healthClient.isHealthy("https://ai.evolua.test/health")).thenReturn(false);

    var status = service.status();

    assertThat(status).hasSize(4);
    assertThat(status.get(0).label()).isEqualTo("Sistema operacional");
    assertThat(status.get(0).state()).isEqualTo("OPERATIONAL");
    assertThat(status.get(1).label()).isEqualTo("IA operacional");
    assertThat(status.get(1).state()).isEqualTo("UNKNOWN");
  }
}
