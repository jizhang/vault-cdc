package org.ezalori.morph.web.controller;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.ezalori.morph.web.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  @GetMapping("/current")
  public Map<String, Object> current(@AuthenticationPrincipal User user) {
    return Map.of("id", user.getId(), "username", user.getUsername());
  }
}
