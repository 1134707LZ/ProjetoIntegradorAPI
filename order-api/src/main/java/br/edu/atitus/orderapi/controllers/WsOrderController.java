package br.edu.atitus.orderapi.controllers;

import br.edu.atitus.orderapi.dtos.Dtos.*;
import br.edu.atitus.orderapi.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Todas as rotas são PROTEGIDAS (gateway: /ws/orders/**). */
@RestController
@RequestMapping("/ws/orders")
public class WsOrderController {
    private final OrderService service;
    public WsOrderController(OrderService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest body, @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(201).body(service.create(body, userId));
    }

    @GetMapping
    public List<OrderResponse> list(@RequestHeader("X-User-Id") Long userId, @RequestHeader("X-User-Type") Integer type) {
        return service.list(userId, type);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId,
                             @RequestHeader("X-User-Type") Integer type) {
        return service.get(id, userId, type);
    }
}
