package com.qs.api.exception;

public class QuantumsparkUnAuthorizedException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public QuantumsparkUnAuthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

  public QuantumsparkUnAuthorizedException(String message) {
    super(message);
  }
}
