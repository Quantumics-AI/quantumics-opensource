package com.qs.api.exception;

public class SchemaChangeException extends RuntimeException {

  private static final long serialVersionUID = -1950460562089602294L;

  public SchemaChangeException() {}

  public SchemaChangeException(String message) {
    super(message);
  }

  public SchemaChangeException(String message, Throwable cause) {
    super(message, cause);
  }
}
