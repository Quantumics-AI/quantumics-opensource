package com.qs.api.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ApiError {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private final LocalDateTime timestamp;
  private HttpStatus status;
  private int code;
  private String message;
  private String debugMessage;

  public ApiError() {
    timestamp = LocalDateTime.now();
  }

  public ApiError(HttpStatus status) {
    this();
    this.status = status;
    this.code = status.value();
  }

  public ApiError(HttpStatus status, Throwable ex) {
    this();
    this.status = status;
    this.code = status.value();
    this.message = "Unexpected error";
    this.debugMessage = ex.getLocalizedMessage();
  }

  public ApiError(HttpStatus status, String message, Throwable ex) {
    this();
    this.status = status;
    this.code = status.value();
    this.message = message;
    this.debugMessage = ex.getLocalizedMessage();
  }

  public ApiError(HttpStatus status, String message) {
    this();
    this.status = status;
    this.code = status.value();
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(HttpStatus status) {
    this.status = status;
  }

}
