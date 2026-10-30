package br.edu.atitus.productapi.configs;

import br.edu.atitus.productapi.entities.ProductEntity;
import br.edu.atitus.productapi.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Catálogo inicial (mesmo do mock do app) quando a tabela está vazia. Dono: usuário 1 (admin). */
@Configuration
public class ProductSeed {
    private static final String CORES = "#2563EB,#0F172A,#DC2626,#16A34A,#F59E0B,#FFFFFF";

    @Bean
    CommandLineRunner seedProducts(ProductRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            add(repo, "Camisa Dry-Fit Athlex", "Camisa esportiva de secagem rápida, ideal para treinos e jogos. Tecido leve e respirável.", 119.90, "Camisas", "P,M,G,GG", CORES, true);
            add(repo, "Chuteira Society Pro", "Chuteira society com travas de borracha e ótima aderência em grama sintética.", 299.90, "Chuteiras", "38,39,40,41,42,43", "#0F172A,#2563EB,#F59E0B", false);
            add(repo, "Bola de Futsal Oficial", "Bola de futsal costurada à mão, com câmara de baixo quique.", 149.90, "Bolas", "Único", "#FFFFFF,#F59E0B", false);
            add(repo, "Camisa de Time Personalizada", "Monte a camisa do seu time: escolha modelo, cor, nome e número.", 139.90, "Camisas", "P,M,G,GG", CORES, true);
            add(repo, "Jaqueta Corta-Vento", "Jaqueta leve, impermeável, com bolsos com zíper e capuz ajustável.", 219.90, "Agasalhos", "P,M,G,GG", "#0F172A,#2563EB,#16A34A", true);
            add(repo, "Mochila Esportiva 30L", "Mochila com compartimento para chuteira e bolso térmico.", 129.90, "Acessórios", "Único", "#0F172A,#DC2626", false);
            add(repo, "Meião Antiderrapante", "Meião com sola antiderrapante e compressão leve.", 39.90, "Acessórios", "P,M,G", CORES, false);
            add(repo, "Calção Treino Flex", "Calção elástico com bolso lateral, perfeito para treinos intensos.", 79.90, "Camisas", "P,M,G,GG", "#0F172A,#2563EB,#DC2626", false);
        };
    }

    private static void add(ProductRepository r, String n, String d, double p, String c, String s, String col, boolean cust) {
        var e = new ProductEntity();
        e.setName(n); e.setDescription(d); e.setPrice(p); e.setCategory(c);
        e.setSizes(s); e.setColors(col); e.setCustomizable(cust); e.setOwnerId(1L);
        r.save(e);
    }
}
