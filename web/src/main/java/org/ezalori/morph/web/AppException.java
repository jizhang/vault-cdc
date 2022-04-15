package org.ezalori.morph.web;

public class AppException extends RuntimeException {
  private static final long serialVersionUID = 8741530383116835378L;

  public AppException(String message) {
    super(message);
  }
}
