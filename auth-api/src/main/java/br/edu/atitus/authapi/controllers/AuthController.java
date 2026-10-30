package br.edu.atitus.authapi.controllers;

import br.edu.atitus.authapi.dtos.Dtos.*;
import br.edu.atitus.authapi.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/** Rotas públicas (via gateway: /auth/**). */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest body) {
        return ResponseEntity.status(201).body(service.signup(body));
    }

    @PostMapping("/signin")
    public SigninResponse signin(@RequestBody SigninRequest body) {
        return service.signin(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handle(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", String.valueOf(e.getReason())));
    }
}
