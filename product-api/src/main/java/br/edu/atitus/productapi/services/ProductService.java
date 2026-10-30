package br.edu.atitus.productapi.services;

import br.edu.atitus.productapi.dtos.Dtos.*;
import br.edu.atitus.productapi.entities.ProductEntity;
import br.edu.atitus.productapi.repositories.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {
    static final Set<String> CATEGORIES = Set.of("Camisas", "Chuteiras", "Bolas", "Acessórios", "Agasalhos");

    private final ProductRepository repo;
    private final AuthorizationService auth;

    public ProductService(ProductRepository repo, AuthorizationService auth) {
        this.repo = repo; this.auth = auth;
    }

    public List<ProductResponse> search(String q, String category) {
        // Nunca passar null: no PostgreSQL, "lower(:q)" com null vira lower(bytea) e dá 500.
        String query = "%" + (q == null ? "" : q.trim().toLowerCase()) + "%";
        String cat = category == null || category.isBlank() || category.equals("Todos") ? "" : category;
        return repo.search(query, cat).stream().map(ProductService::toResponse).toList();
    }

    public ProductResponse get(Long id) { return toResponse(find(id)); }

    public List<ProductResponse> mine(Long userId) {
        return repo.findByOwnerIdOrderByIdDesc(userId).stream().map(ProductService::toResponse).toList();
    }

    public ProductResponse create(ProductRequest r, Long userId) {
        auth.requireAuthenticated(userId);
        var p = new ProductEntity();
        apply(p, r);
        p.setOwnerId(userId);
        return toResponse(repo.save(p));
    }

    public ProductResponse update(Long id, ProductRequest r, Long userId, Integer type) {
        var p = find(id);
        auth.requireOwnerOrAdmin(p.getOwnerId(), userId, type);
        apply(p, r);
        return toResponse(repo.save(p));
    }

    public void delete(Long id, Long userId, Integer type) {
        var p = find(id);
        auth.requireOwnerOrAdmin(p.getOwnerId(), userId, type);
        repo.delete(p);
    }

    // ---------- validação (400) ----------
    private void apply(ProductEntity p, ProductRequest r) {
        if (r == null) throw bad("Requisição inválida");
        if (r.nome() == null || r.nome().isBlank()) throw bad("Título inválido");
        if (r.descricao() == null || r.descricao().isBlank()) throw bad("Descrição inválida");
        if (r.preco() == null || r.preco() <= 0) throw bad("Preço inválido");
        if (r.categoria() == null || !CATEGORIES.contains(r.categoria())) throw bad("Categoria inválida");
        p.setName(r.nome().trim());
        p.setDescription(r.descricao().trim());
        p.setPrice(r.preco());
        p.setCategory(r.categoria());
        p.setSizes(join(r.tamanhos(), "Único"));
        p.setColors(join(r.cores(), "#0F172A"));
        p.setImage(r.imagem() != null && !r.imagem().isBlank() ? r.imagem().trim() : null);
        p.setCustomizable(Boolean.TRUE.equals(r.personalizavel()));
    }

    private ProductEntity find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    private static String join(List<String> l, String def) {
        var clean = l == null ? List.<String>of() : l.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).toList();
        return clean.isEmpty() ? def : String.join(",", clean);
    }
    private static List<String> split(String s) {
        return s == null || s.isBlank() ? List.of() : Arrays.stream(s.split(",")).map(String::trim).toList();
    }

    public static ProductResponse toResponse(ProductEntity p) {
        return new ProductResponse(String.valueOf(p.getId()), p.getName(), p.getDescription(), p.getPrice(), p.getCategory(),
                split(p.getSizes()), split(p.getColors()), p.getImage(), p.isCustomizable(),
                p.getOwnerId() == null ? null : String.valueOf(p.getOwnerId()));
    }
}
