package org.ezalori.morph.web.controller;

import java.util.Map;

import lombok.Data;

@Data
public class ApiResponse {
  private int code = 200;
  private Map<String, Object> payload = null;

  ApiResponse(Map<String, Object> payload) {
    this.payload = payload;
  }
}
