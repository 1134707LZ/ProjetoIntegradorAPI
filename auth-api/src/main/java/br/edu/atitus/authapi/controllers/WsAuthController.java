package br.edu.atitus.authapi.controllers;

import br.edu.atitus.authapi.dtos.Dtos.UserResponse;
import br.edu.atitus.authapi.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

/** Rotas protegidas (via gateway: /ws/auth/**). Autorização: somente administrador lista usuários. */
@RestController
@RequestMapping("/ws/auth")
public class WsAuthController {
    private final UserRepository repo;
    public WsAuthController(UserRepository repo) { this.repo = repo; }

    @GetMapping("/users")
    public List<UserResponse> users(@RequestHeader("X-User-Type") Integer type) {
        if (type == null || type != 0)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem permissão para esta operação");
        return repo.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getType())).toList();
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader("X-User-Id") Long id) {
        return repo.findById(id).map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getType()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handle(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", String.valueOf(e.getReason())));
    }
}
