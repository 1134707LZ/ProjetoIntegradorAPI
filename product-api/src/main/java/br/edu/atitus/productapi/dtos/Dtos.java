package br.edu.atitus.productapi.dtos;

import java.util.List;

/** Contrato idêntico ao tipo `Produto` do app React Native. */
public final class Dtos {
    private Dtos() {}

    public record ProductRequest(String nome, String descricao, Double preco, String categoria,
                                 List<String> tamanhos, List<String> cores, String imagem,
                                 Boolean personalizavel) {}

    public record ProductResponse(String id, String nome, String descricao, double preco, String categoria,
                                  List<String> tamanhos, List<String> cores, String imagem,
                                  boolean personalizavel, String vendedorId) {}
}
