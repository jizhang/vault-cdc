package org.ezalori.morph.web.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ApiException.class)
  public ApiResponse handleApiException(Exception e) {
    ApiResponse resp = new ApiResponse(Map.of("message", e.getMessage()));
    resp.setCode(400);
    return resp;
  }
}
