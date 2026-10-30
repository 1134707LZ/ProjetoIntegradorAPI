package br.edu.atitus.gatewayapi.configs;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator getRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Rotas públicas
                .route("auth-api", r -> r.path("/auth/**").uri("lb://auth-api"))
                .route("product-api", r -> r.path("/products/**").uri("lb://product-api"))

                // Rotas protegidas (/ws/**): o AuthFilter exige JWT válido
                .route("product-api-ws", r -> r.path("/ws/products/**").uri("lb://product-api"))
                .route("order-api-ws", r -> r.path("/ws/orders/**").uri("lb://order-api"))
                .route("auth-api-ws", r -> r.path("/ws/auth/**").uri("lb://auth-api"))
                .build();
    }
}
