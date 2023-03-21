package com.qs.api.exception;

public class QuantumsparkAwsOperationFailedException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = -971061793313868621L;

  public QuantumsparkAwsOperationFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public QuantumsparkAwsOperationFailedException(String message) {
    super(message);
  }
}
