package br.edu.atitus.gatewayapi;

import br.edu.atitus.gatewayapi.components.JwtUtil;
import br.edu.atitus.gatewayapi.filters.AuthFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AuthFilterTest {
    static final String SECRET = "chaveSuperSecretaParaJWTdeExemplo!@#123";
    final AuthFilter filter = new AuthFilter(new JwtUtil(SECRET), "segredo-gateway-teste");

    String token(String secret, int type, long expMs) {
        return Jwts.builder().claim("id", 5L).claim("type", type).claim("email", "u@x.com")
                .expiration(new Date(System.currentTimeMillis() + expMs))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
    }

    MockServerWebExchange run(String path, String bearer, AtomicReference<org.springframework.http.HttpHeaders> seen) {
        var req = MockServerHttpRequest.get(path).header("X-User-Type", "0");   // tentativa de forjar admin
        if (bearer != null) req.header("Authorization", "Bearer " + bearer);
        var ex = MockServerWebExchange.from(req);
        filter.filter(ex, e -> { seen.set(e.getRequest().getHeaders()); return Mono.empty(); }).block();
        return ex;
    }

    @Test void publicaPassaSemIdentidadeEremoveCabecalhoForjado() {
        var seen = new AtomicReference<org.springframework.http.HttpHeaders>();
        run("/products", null, seen);
        assertNotNull(seen.get());
        assertNull(seen.get().getFirst("X-User-Type"));
    }

    @Test void gatewayInjetaSegredoESobrescreveValorForjadoDoCliente() {
        var seen = new AtomicReference<org.springframework.http.HttpHeaders>();
        var req = MockServerHttpRequest.get("/products").header("X-Gateway-Secret", "forjado");
        var ex = MockServerWebExchange.from(req);
        filter.filter(ex, e -> { seen.set(e.getRequest().getHeaders()); return Mono.empty(); }).block();
        assertEquals("segredo-gateway-teste", seen.get().getFirst("X-Gateway-Secret"));
        assertEquals(1, seen.get().get("X-Gateway-Secret").size());
    }

    @Test void protegidaSemTokenRetorna401() {
        var seen = new AtomicReference<org.springframework.http.HttpHeaders>();
        var ex = run("/ws/products", null, seen);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        assertNull(seen.get());
    }

    @Test void tokenValidoPropagaIdentidadeDoTokenENaoDoCliente() {
        var seen = new AtomicReference<org.springframework.http.HttpHeaders>();
        run("/ws/products", token(SECRET, 1, 60000), seen);
        assertEquals("1", seen.get().getFirst("X-User-Type"));   // não "0" forjado
        assertEquals("5", seen.get().getFirst("X-User-Id"));
        assertEquals("u@x.com", seen.get().getFirst("X-User-Email"));
    }

    @Test void assinaturaInvalidaOuExpiradoRetorna401() {
        var seen = new AtomicReference<org.springframework.http.HttpHeaders>();
        assertEquals(HttpStatus.UNAUTHORIZED, run("/ws/orders", token("outraChaveCompletamenteDiferente-1234567890", 0, 60000), seen).getResponse().getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, run("/ws/orders", token(SECRET, 0, -1000), seen).getResponse().getStatusCode());
    }
}
