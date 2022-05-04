package org.ezalori.morph.web.controller;

import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.ezalori.morph.web.AppException;
import org.ezalori.morph.web.form.LoginForm;
import org.ezalori.morph.web.model.User;
import org.ezalori.morph.web.utils.FormUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
  @PostMapping("/login")
  public Map<String, Object> login(@Valid LoginForm form, BindingResult bindingResult,
                                   HttpServletRequest request) {
    FormUtils.checkBindingErrors(bindingResult);

    try {
      request.login(form.getUsername(), form.getPassword());
    } catch (ServletException e) {
      throw new AppException("Invalid username or password.");
    }

    var auth = (Authentication) request.getUserPrincipal();
    var user = (User) auth.getPrincipal();
    return user.toCurrentUser();
  }

  @PostMapping("/logout")
  public Map<String, Object> logout(HttpServletRequest request) throws ServletException {
    request.logout();
    return Map.of();
  }

  @GetMapping("/current-user")
  public Map<String, Object> current(@AuthenticationPrincipal User user) {
    return user.toCurrentUser();
  }
}
