package br.edu.atitus.orderapi.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Aceita rotas protegidas (/ws/**) somente quando vierem do gateway, que injeta X-Gateway-Secret.
 * Impede que alguém chame o microservice direto na porta forjando X-User-Id / X-User-Type.
 */
@Component
public class GatewayOnlyFilter extends OncePerRequestFilter {

    private final byte[] secret;

    public GatewayOnlyFilter(@Value("${gateway.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/ws/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String received = req.getHeader("X-Gateway-Secret");
        if (received != null && MessageDigest.isEqual(secret, received.getBytes(StandardCharsets.UTF_8))) {
            chain.doFilter(req, res);
            return;
        }
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\":\"Acesso permitido apenas via gateway\"}");
    }
}
