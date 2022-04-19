package org.ezalori.morph.web;

import lombok.Getter;

public class AppException extends RuntimeException {
  private static final long serialVersionUID = 8741530383116835378L;

  @Getter
  private int status;

  public AppException(String message) {
    this(400, message);
  }

  public AppException(int status, String message) {
    super(message);
    this.status = status;
  }
}
