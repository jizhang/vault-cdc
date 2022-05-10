package org.ezalori.morph.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.Value;
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
  @Operation(summary = "Login user.")
  @PostMapping("/login")
  public CurrentUser login(@Valid LoginForm form, BindingResult bindingResult,
      HttpServletRequest request) {
    FormUtils.checkBindingErrors(bindingResult);

    try {
      request.login(form.getUsername(), form.getPassword());
    } catch (ServletException e) {
      throw new AppException("Invalid username or password.");
    }

    var auth = (Authentication) request.getUserPrincipal();
    var user = (User) auth.getPrincipal();
    return toCurrentUser(user);
  }

  @Operation(summary = "Logout user.")
  @PostMapping("/logout")
  public LogoutResponse logout(HttpServletRequest request) throws ServletException {
    request.logout();
    return new LogoutResponse();
  }

  @Operation(summary = "Get current logged-in user.")
  @GetMapping("/current-user")
  public CurrentUser current(@AuthenticationPrincipal User user) {
    return toCurrentUser(user);
  }

  private CurrentUser toCurrentUser(User user) {
    return new CurrentUser(user.getId(), user.getUsername());
  }

  @Value
  static class CurrentUser {
    int id;
    String username;
  }

  @Value
  static class LogoutResponse {}
}
