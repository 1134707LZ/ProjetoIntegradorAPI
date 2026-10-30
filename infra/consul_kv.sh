#!/bin/sh
# Grava as configs no Consul já em execução (docker compose up -d)
# Inicia o Consul em modo dev e grava as configurações externas (KV) dos microservices.

# auth-api
docker compose exec -T consul consul kv put config/auth-api/server.port "8200"
docker compose exec -T consul consul kv put config/auth-api/spring.datasource.url "jdbc:postgresql://localhost/db_user"
docker compose exec -T consul consul kv put config/auth-api/spring.datasource.username "postgres"
docker compose exec -T consul consul kv put config/auth-api/spring.datasource.password "postgres"
docker compose exec -T consul consul kv put config/auth-api/jwt.secret "chaveSuperSecretaParaJWTdeExemplo!@#123"

# product-api
docker compose exec -T consul consul kv put config/product-api/server.port "8100"
docker compose exec -T consul consul kv put config/product-api/spring.datasource.url "jdbc:postgresql://localhost/db_product"
docker compose exec -T consul consul kv put config/product-api/spring.datasource.username "postgres"
docker compose exec -T consul consul kv put config/product-api/spring.datasource.password "postgres"

# order-api
docker compose exec -T consul consul kv put config/order-api/server.port "8400"
docker compose exec -T consul consul kv put config/order-api/spring.datasource.url "jdbc:postgresql://localhost/db_order"
docker compose exec -T consul consul kv put config/order-api/spring.datasource.username "postgres"
docker compose exec -T consul consul kv put config/order-api/spring.datasource.password "postgres"

# gateway-api (mesma chave do auth-api!)
docker compose exec -T consul consul kv put config/gateway-api/jwt.secret "chaveSuperSecretaParaJWTdeExemplo!@#123"

# Segredo gateway -> microservices (compartilhado; troque por um valor seu)
docker compose exec -T consul consul kv put config/application/gateway.secret "troque-este-segredo-do-gateway-0123456789"
