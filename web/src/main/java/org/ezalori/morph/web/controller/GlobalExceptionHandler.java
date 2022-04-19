package org.ezalori.morph.web.controller;

import java.util.Map;

import org.ezalori.morph.web.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(AppException.class)
  public Map<String, Object> handleAppException(AppException e) {
    return Map.of("status", e.getStatus(), "message", e.getMessage());
  }
}
