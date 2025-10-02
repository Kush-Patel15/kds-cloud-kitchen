package com.cloudkitchen.controller;

import com.cloudkitchen.model.User;
import com.cloudkitchen.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin // Add this annotation
public class AuthController {

  private final UserService userService;
  public AuthController(UserService userService) { this.userService = userService; }

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@RequestBody Map<String,String> body) {
    String username = body.get("username");
    String email = body.get("email");
    String password = body.get("password");
    String roleRaw = body.getOrDefault("role","CUSTOMER_SERVICE");
    if (username==null || email==null || password==null)
      return ResponseEntity.badRequest().body(Map.of("success",false,"message","Missing fields"));
    if (userService.findByEmail(email)!=null)
      return ResponseEntity.status(409).body(Map.of("success",false,"message","Email in use"));
    if (userService.findByUsername(username)!=null)
      return ResponseEntity.status(409).body(Map.of("success",false,"message","Username in use"));

    User u = new User();
    u.setUsername(username);
    u.setEmail(email);
    u.setPassword(password);
    try { u.setRole(User.Role.valueOf(roleRaw)); } catch (Exception ignored) {}
    User saved = userService.create(u);

    return ResponseEntity.ok(Map.of("success",true,"user",Map.of(
        "id",saved.getId(),"username",saved.getUsername(),"email",saved.getEmail(),"role",saved.getRole().name()
    )));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
    String email = body.get("email");
    String password = body.get("password");
    if (email==null || password==null)
      return ResponseEntity.badRequest().body(Map.of("success",false,"message","Email & password required"));
    if (!userService.validateLogin(email,password))
      return ResponseEntity.status(401).body(Map.of("success",false,"message","Invalid credentials"));
    User u = userService.findByEmail(email);
    return ResponseEntity.ok(Map.of("success",true,"user",Map.of(
        "id",u.getId(),"username",u.getUsername(),"email",u.getEmail(),"role",u.getRole().name()
    )));
  }
}