package br.edu.atitus.productapi.controllers;

import br.edu.atitus.productapi.dtos.Dtos.ProductResponse;
import br.edu.atitus.productapi.services.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Rotas PÚBLICAS (gateway: /products/**). Não dependem de X-User-*. */
@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }

    @GetMapping
    public List<ProductResponse> list(@RequestParam(required = false) String busca,
                                      @RequestParam(required = false) String categoria) {
        return service.search(busca, categoria);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) { return service.get(id); }
}
