package org.ezalori.morph.web.controller;

import lombok.Value;
import org.ezalori.morph.web.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(AppException.class)
  public AppExceptionResponse handleAppException(AppException e) {
    return new AppExceptionResponse(e.getStatus(), e.getMessage());
  }

  @Value
  public static class AppExceptionResponse {
    int status;
    String message;
  }
}
