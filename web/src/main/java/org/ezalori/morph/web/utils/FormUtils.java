package org.ezalori.morph.web.utils;

import org.ezalori.morph.web.AppException;
import org.springframework.validation.BindingResult;

public class FormUtils {
  public static void checkBindingErrors(BindingResult bindingResult) {
    if (!bindingResult.hasErrors()) {
      return;
    }

    var message = new StringBuilder();
    for (var error : bindingResult.getGlobalErrors()) {
      message.append(error.getObjectName())
          .append(": ").append(error.getDefaultMessage())
          .append("\n");
    }

    for (var error : bindingResult.getFieldErrors()) {
      message.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("\n");
    }

    throw new AppException(message.toString());
  }
}
