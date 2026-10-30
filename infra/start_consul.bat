@echo off
REM Inicia o Consul em modo dev e grava as configurações externas (KV) dos microservices.
start "consul" consul agent -dev
timeout /t 5 >nul

REM auth-api
consul kv put config/auth-api/server.port "8200"
consul kv put config/auth-api/spring.datasource.url "jdbc:postgresql://localhost/db_user"
consul kv put config/auth-api/spring.datasource.username "postgres"
consul kv put config/auth-api/spring.datasource.password "postgres"
consul kv put config/auth-api/jwt.secret "chaveSuperSecretaParaJWTdeExemplo!@#123"

REM product-api
consul kv put config/product-api/server.port "8100"
consul kv put config/product-api/spring.datasource.url "jdbc:postgresql://localhost/db_product"
consul kv put config/product-api/spring.datasource.username "postgres"
consul kv put config/product-api/spring.datasource.password "postgres"

REM order-api
consul kv put config/order-api/server.port "8400"
consul kv put config/order-api/spring.datasource.url "jdbc:postgresql://localhost/db_order"
consul kv put config/order-api/spring.datasource.username "postgres"
consul kv put config/order-api/spring.datasource.password "postgres"

REM gateway-api (mesma chave do auth-api!)
consul kv put config/gateway-api/jwt.secret "chaveSuperSecretaParaJWTdeExemplo!@#123"

REM Segredo gateway -> microservices (compartilhado por todos; troque por um valor seu)
consul kv put config/application/gateway.secret "troque-este-segredo-do-gateway-0123456789"
