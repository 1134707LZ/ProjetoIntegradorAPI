package br.edu.atitus.gatewayapi.filters;

import br.edu.atitus.gatewayapi.components.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final List<String> PROTECTED_ROUTES = List.of("/ws/");
    private static final List<String> USER_HEADERS = List.of("X-User-Id", "X-User-Type", "X-User-Email");

    /** Segredo compartilhado gateway -> microservices: prova que a requisição passou pelo gateway. */
    public static final String GATEWAY_HEADER = "X-Gateway-Secret";

    private final JwtUtil jwtUtil;
    private final String gatewaySecret;

    public AuthFilter(JwtUtil jwtUtil, @Value("${gateway.secret}") String gatewaySecret) {
        this.jwtUtil = jwtUtil;
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Nunca confiar em cabeçalhos de identidade enviados pelo cliente.
        ServerHttpRequest sanitized = request.mutate()
                .headers(h -> {
                    USER_HEADERS.forEach(h::remove);
                    h.set(GATEWAY_HEADER, gatewaySecret);   // sobrescreve qualquer valor vindo do cliente
                }).build();

        String path = request.getURI().getPath();

        // Rota pública: segue sem identidade.
        if (PROTECTED_ROUTES.stream().noneMatch(path::startsWith)) {
            return chain.filter(exchange.mutate().request(sanitized).build());
        }

        // Rota protegida: exige Bearer JWT válido.
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            Claims claims = jwtUtil.validateToken(auth.substring(7));
            if (claims != null) {
                Long id = claims.get("id", Long.class);
                Integer type = claims.get("type", Integer.class);
                String email = claims.get("email", String.class);
                if (id != null && type != null && email != null) {
                    ServerHttpRequest authenticated = sanitized.mutate().headers(h -> {
                        h.set("X-User-Id", id.toString());
                        h.set("X-User-Type", type.toString());
                        h.set("X-User-Email", email);
                    }).build();
                    return chain.filter(exchange.mutate().request(authenticated).build());
                }
            }
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
