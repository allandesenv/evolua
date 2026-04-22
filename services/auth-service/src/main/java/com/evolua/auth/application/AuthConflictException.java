package com.evolua.auth.application;

public class AuthConflictException extends RuntimeException {
  public AuthConflictException(String message) {
    super(message);
  }
}
