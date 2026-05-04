package com.evolua.auth.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.evolua.auth.application.AuthConflictException;
import com.evolua.auth.application.InvalidCredentialsException;
import com.evolua.auth.application.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldMapUserNotFoundToUnauthorized() {
    var response = handler.handleUserNotFound(new UserNotFoundException("Usuario nao existe."));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody().details()).containsExactly("Credenciais invalidas.");
  }

  @Test
  void shouldMapInvalidCredentialsToUnauthorized() {
    var response =
        handler.handleInvalidCredentials(
            new InvalidCredentialsException("Credenciais invalidas."));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody().details()).containsExactly("Credenciais invalidas.");
  }

  @Test
  void shouldMapConflictToConflictStatus() {
    var response = handler.handleConflict(new AuthConflictException("Email ja cadastrado."));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().details()).containsExactly("Email ja cadastrado.");
  }
}
