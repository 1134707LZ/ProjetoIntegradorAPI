# Athlex Backend — Microservices Java (Spring Boot 4 / Spring Cloud)

Gateway + Autenticação (JWT) + Autorização + Integração, para o app React Native Athlex.

```
App ──► gateway-api :8080 ──(Consul / lb://)──► auth-api    :8200  (db_user)
          valida JWT                         ├─► product-api :8100  (db_product)
          propaga X-User-*                   └─► order-api   :8400  (db_order) ──► product-api (preço)
```

Stack: Java 21, Spring Boot 4.1.0, Spring Cloud 2025.1.4 (Gateway WebFlux, Consul Config + Discovery), PostgreSQL, JJWT 0.13.

## Pré-requisitos
Java 21, PostgreSQL 16, Consul 1.20 e (opcional) Docker. Maven vem embutido no IntelliJ.

## Variáveis e segredos (sem valor padrão no código)
Os serviços **não sobem** sem estes valores. Cada um pode vir do **Consul KV** (gravado pelos scripts de `infra/`) ou de **variável de ambiente**.

| Chave no Consul KV | Variável de ambiente | Quem usa | Observação |
|---|---|---|---|
| `config/auth-api/jwt.secret` | `JWT_SECRET` | auth-api | assina o JWT (HS256, ≥ 32 caracteres) |
| `config/gateway-api/jwt.secret` | `JWT_SECRET` | gateway-api | **igual** ao do auth-api (valida o JWT) |
| `config/application/gateway.secret` | `GATEWAY_SECRET` | gateway + os 3 serviços | prova que a requisição passou pelo gateway |
| `config/<serviço>/spring.datasource.url/username/password` | `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | auth, product, order | padrão: `localhost:5432`, `postgres`/`postgres` |
| – | `DISCOVERY_IP` | todos | IP registrado no Consul (padrão `127.0.0.1`) |

Os scripts `infra/start_consul.bat` (Windows) e `infra/consul_kv.sh` (Docker) gravam valores de **desenvolvimento**. Troque-os em produção.

## Subir o ambiente
**Ordem:** bancos → Consul (com as chaves) → auth-api → product-api → order-api → gateway-api.

**A) Windows sem Docker**
1. Criar os bancos: `CREATE DATABASE db_user; CREATE DATABASE db_product; CREATE DATABASE db_order;`
2. `infra\start_consul.bat` (abre o Consul em modo dev e grava as chaves). Deixar a janela aberta. Conferir em http://localhost:8500 → Key/Value → `config`.
3. Iniciar auth-api, product-api, order-api e, por último, gateway-api (IntelliJ: executar cada classe `*Application`; ou `mvn spring-boot:run` em cada pasta).

**B) Com Docker**
1. `docker compose up -d` (Consul + PostgreSQL com os 3 bancos criados).
2. `sh infra/consul_kv.sh` (grava as chaves).
3. Iniciar os 4 serviços na mesma ordem.

**Conferir:** http://localhost:8500 → Services com os 4 em verde; http://localhost:8080/products lista os produtos. O cliente usa sempre o gateway: **http://localhost:8080**.

Admin inicial (didático): `admin@admin.dev` / `teste`.

## Rotas
| Rota (gateway) | Acesso | Autorização (microservice) |
|---|---|---|
| `POST /auth/signup` `{name,email,phone,password}` | público | cadastro sempre cria usuário comum (type 1) |
| `POST /auth/signin` `{email,password}` → `{token,user}` | público | 401 se credencial inválida |
| `GET /products?busca=&categoria=`, `GET /products/{id}` | público | – |
| `POST /ws/products` | JWT | qualquer autenticado (vira dono) |
| `GET /ws/products/mine` | JWT | só os seus |
| `PUT/DELETE /ws/products/{id}` | JWT | **dono ou admin** → senão 403 |
| `POST /ws/orders` | JWT | preço recalculado no servidor via product-api |
| `GET /ws/orders`, `GET /ws/orders/{id}` | JWT | próprios pedidos; admin vê todos; terceiros → 403 |
| `GET /ws/auth/users` | JWT | **somente admin** |
| `GET /ws/auth/me` | JWT | – |

`/ws/**` = protegido pelo `AuthFilter` do gateway (401 sem/ com token inválido). Autenticação no gateway; autorização nos microservices. O filtro remove `X-User-*` enviados pelo cliente (anti-spoofing) e os recria a partir do JWT (`id`, `type`, `email`). type: `0` admin, `1` comum.

## Roteiro de testes (Postman, opcional)
| # | Requisição | Esperado |
|---|---|---|
| 1 | `POST /auth/signin` admin | 200 + token |
| 2 | `POST /ws/products` sem token | 401 |
| 3 | `POST /ws/products` token admin | 201 |
| 4 | `POST /auth/signup` + `signin` usuário comum | 201 / 200 |
| 5 | `GET /ws/auth/users` token comum | **403** |
| 6 | `GET /ws/auth/users` token admin | 200 |
| 7 | `DELETE /ws/products/{id do admin}` token comum | **403** |
| 8 | `DELETE /ws/products/{id do comum}` token comum | 204 |
| 9 | `POST /ws/orders` (itens + endereço + pagamento) | 201, total calculado no servidor |
| 10 | `GET /ws/orders` token comum | só os seus |
| 11 | `GET /products?busca=camisa` sem token | 200 |
| 12 | `POST /ws/products` com `X-User-Type: 0` forjado + token comum | cabeçalho ignorado |

Testes automatizados: `gateway-api` (`AuthFilterTest`: pública, 401, anti-spoofing, assinatura/expiração) e `auth-api` (`JwtServiceTest`). `cd gateway-api && ./mvnw test`.

## Limitações conhecidas (citar no relatório)
- Em produção, restringir também por rede (firewall/VPC) o acesso direto aos microservices; hoje a proteção é o segredo `X-Gateway-Secret`.
- Os valores de desenvolvimento gravados em `infra/` (chave JWT, segredo do gateway, senha do admin) devem ser trocados em produção.
- Imagens de produto ficam no banco como data URI base64 (simples; em produção usar storage de objetos).
- `ddl-auto=update` para facilitar o laboratório (em produção, migrations).

## Proteção contra acesso direto
O gateway injeta `X-Gateway-Secret` em toda requisição encaminhada; as rotas `/ws/**` dos microservices respondem **401** sem ele,
então chamar `:8100/:8200/:8400` direto com `X-User-*` forjado não funciona. Rotas públicas (`/products`, `/auth/signin`, `/auth/signup`) seguem abertas.
O order-api usa a porta **8400** (a 8300 conflita com o Consul nativo).

## Windows: nome do PC com acento

O registro no Consul usa o IP (`spring.cloud.consul.discovery.prefer-ip-address=true`, padrão `127.0.0.1`, ajustável pela env `DISCOVERY_IP`).
Sem isso, um nome de máquina com acento (ex.: `Vinícius`) gera URL inválida no gateway e `GET /products` retorna 500.
