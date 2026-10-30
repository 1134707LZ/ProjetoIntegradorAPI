package br.edu.atitus.orderapi.dtos;

import java.util.List;

/** Contrato idêntico ao app (api.criarPedido / tipo Pedido). */
public final class Dtos {
    private Dtos() {}

    public record AddressDto(String cep, String rua, String numero, String cidade, String uf) {}
    public record CustomizationDto(String modelo, String cor, String nome, String numero, Double acrescimo) {}
    public record ItemRequest(Long produtoId, Integer quantidade, String tamanho, String cor, CustomizationDto personalizacao) {}
    public record OrderRequest(AddressDto endereco, String pagamento, List<ItemRequest> itens) {}

    public record ItemResponse(String nome, int quantidade, double preco) {}
    public record OrderResponse(String id, String data, double total, String status, List<ItemResponse> itens) {}

    /** Subconjunto do produto retornado pelo product-api. */
    public record ProductInfo(String id, String nome, double preco, boolean personalizavel) {}
}
