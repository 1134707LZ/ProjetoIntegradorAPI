package br.edu.atitus.productapi.controllers;

import br.edu.atitus.productapi.dtos.Dtos.*;
import br.edu.atitus.productapi.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Rotas PROTEGIDAS (gateway: /ws/products/** — JWT validado no gateway). */
@RestController
@RequestMapping("/ws/products")
public class WsProductController {
    private final ProductService service;
    public WsProductController(ProductService service) { this.service = service; }

    /** Qualquer usuário autenticado pode anunciar; o dono é o X-User-Id. */
    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest dto,
                                                  @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(201).body(service.create(dto, userId));
    }

    @GetMapping("/mine")
    public List<ProductResponse> mine(@RequestHeader("X-User-Id") Long userId) {
        return service.mine(userId);
    }

    /** Somente o dono ou um administrador. */
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest dto,
                                  @RequestHeader("X-User-Id") Long userId,
                                  @RequestHeader("X-User-Type") Integer type) {
        return service.update(id, dto, userId, type);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("X-User-Id") Long userId,
                                       @RequestHeader("X-User-Type") Integer type) {
        service.delete(id, userId, type);
        return ResponseEntity.noContent().build();
    }
}
