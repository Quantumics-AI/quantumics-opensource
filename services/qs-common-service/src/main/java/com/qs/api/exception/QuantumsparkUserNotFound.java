package com.qs.api.exception;

public class QuantumsparkUserNotFound extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public QuantumsparkUserNotFound(String message, Throwable cause) {
    super(message, cause);
  }

  public QuantumsparkUserNotFound(String message) {
    super(message);
  }
}
