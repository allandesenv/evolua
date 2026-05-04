package com.evolua.auth.interfaces.rest;

import com.evolua.auth.application.AuthConflictException;
import com.evolua.auth.application.InvalidCredentialsException;
import com.evolua.auth.application.UserNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
    return buildError(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.");
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(
      InvalidCredentialsException exception) {
    return buildError(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.");
  }

  @ExceptionHandler(AuthConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(AuthConflictException exception) {
    return buildError(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
    return buildError(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
    List<String> details =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(Instant.now(), 400, "Bad Request", details));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ErrorResponse(
                Instant.now(), 500, "Internal Server Error", List.of("Erro inesperado.")));
  }

  private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String detail) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), List.of(detail)));
  }
}
