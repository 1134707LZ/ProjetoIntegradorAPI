package br.edu.atitus.orderapi.services;

import br.edu.atitus.orderapi.dtos.Dtos.*;
import br.edu.atitus.orderapi.entities.OrderEntity;
import br.edu.atitus.orderapi.entities.OrderItemEntity;
import br.edu.atitus.orderapi.repositories.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class OrderService {
    private static final int ADMIN = 0;
    private static final double PRECO_NOME = 10.0;     // acréscimos de personalização (iguais ao app)
    private static final double PRECO_NUMERO = 10.0;
    private static final Set<String> PAGAMENTOS = Set.of("CARTAO", "PIX", "BOLETO");

    private final OrderRepository repo;
    private final RestClient products;

    public OrderService(OrderRepository repo, RestClient.Builder lbBuilder) {
        this.repo = repo;
        this.products = lbBuilder.baseUrl("http://product-api").build();
    }

    public OrderResponse create(OrderRequest r, Long userId) {
        if (r == null) throw bad("Requisição inválida");
        validateAddress(r.endereco());
        if (r.pagamento() == null || !PAGAMENTOS.contains(r.pagamento())) throw bad("Forma de pagamento inválida");
        if (r.itens() == null || r.itens().isEmpty()) throw bad("Carrinho vazio");

        var order = new OrderEntity();
        order.setUserId(userId);
        order.setPayment(r.pagamento());
        order.setStatus(r.pagamento().equals("CARTAO") ? "EM_TRANSITO" : "AGUARDANDO_PAGAMENTO");
        var a = r.endereco();
        order.setZip(a.cep()); order.setStreet(a.rua()); order.setNumber(a.numero());
        order.setCity(a.cidade()); order.setState(a.uf());

        double total = 0;
        for (var i : r.itens()) {
            if (i.produtoId() == null || i.quantidade() == null || i.quantidade() < 1) throw bad("Item inválido");
            ProductInfo p = fetchProduct(i.produtoId());   // preço SEMPRE vem do product-api, nunca do cliente

            double extra = 0;
            var c = i.personalizacao();
            if (c != null) {
                if (!p.personalizavel()) throw bad("Produto não aceita personalização: " + p.nome());
                if (c.nome() != null && !c.nome().isBlank()) extra += PRECO_NOME;
                if (c.numero() != null && !c.numero().isBlank()) extra += PRECO_NUMERO;
            }
            var it = new OrderItemEntity();
            it.setProductId(i.produtoId());
            it.setProductName(p.nome());
            it.setUnitPrice(p.preco() + extra);
            it.setQuantity(i.quantidade());
            it.setSize(i.tamanho());
            it.setColor(i.cor());
            if (c != null) { it.setCustomModel(c.modelo()); it.setCustomName(c.nome()); it.setCustomNumber(c.numero()); }
            order.getItems().add(it);
            total += it.getUnitPrice() * it.getQuantity();
        }
        order.setTotal(Math.round(total * 100) / 100.0);
        return toResponse(repo.save(order));
    }

    /** Usuário vê apenas os próprios pedidos; administrador vê todos. */
    public List<OrderResponse> list(Long userId, Integer type) {
        var list = (type != null && type == ADMIN) ? repo.findAllByOrderByIdDesc() : repo.findByUserIdOrderByIdDesc(userId);
        return list.stream().map(OrderService::toResponse).toList();
    }

    public OrderResponse get(Long id, Long userId, Integer type) {
        var o = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        boolean admin = type != null && type == ADMIN;
        if (!admin && !o.getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é o dono deste recurso");
        return toResponse(o);
    }

    private ProductInfo fetchProduct(Long id) {
        try {
            return products.get().uri("/products/{id}", id).retrieve().body(ProductInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw bad("Produto " + id + " não existe");
        }
    }

    private void validateAddress(AddressDto a) {
        if (a == null || a.cep() == null || a.cep().replaceAll("\\D", "").length() != 8) throw bad("CEP inválido");
        if (blank(a.rua()) || blank(a.numero()) || blank(a.cidade())) throw bad("Endereço incompleto");
        if (a.uf() == null || a.uf().trim().length() != 2) throw bad("UF inválida");
    }
    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    static OrderResponse toResponse(OrderEntity o) {
        return new OrderResponse("PED-" + (1000 + o.getId()), o.getCreatedAt().toString(), o.getTotal(), o.getStatus(),
                o.getItems().stream().map(i -> new ItemResponse(i.getProductName(), i.getQuantity(), i.getUnitPrice())).toList());
    }
}
