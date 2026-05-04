# Evolua Backend

Backend em microsservicos do Evolua com Java 21, Spring Boot 3.5, Maven, Docker Compose e bancos separados por contexto.

## Servicos

- `gateway-service`: entrada HTTP principal
- `auth-service`: login, cadastro, refresh, Google OAuth local
- `user-service`: perfil principal do usuario, avatar e dados pessoais
- `content-service`: trilhas, jornadas privadas e gate premium
- `emotional-service`: check-ins emocionais
- `social-service`: reflexoes e espacos
- `chat-service`: mensagens e STOMP/WebSocket
- `subscription-service`: planos, checkout e webhook de pagamento
- `notification-service`: inbox in-app e console admin
- `ai-service`: insights, jornada e conversa contextual

## Requisitos locais

- Docker Desktop
- Java 21
- Maven 3.9+

## Subir tudo localmente

```powershell
docker compose up --build -d
docker compose ps
```

Parar:

```powershell
docker compose down
```

Resetar dados:

```powershell
docker compose down -v
docker compose up --build -d
```

## Portas

- `8080` gateway
- `8081` auth
- `8082` user
- `8083` content
- `8084` emotional
- `8085` social
- `8086` chat
- `8087` subscription
- `8088` notification
- `8089` ai

## Seeds locais

- admin: `clara@evolua.local / 123456`
- usuario comum: `leo@evolua.local / 123456`

## Swagger

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`
- `http://localhost:8085/swagger-ui.html`
- `http://localhost:8086/swagger-ui.html`
- `http://localhost:8087/swagger-ui.html`
- `http://localhost:8088/swagger-ui.html`
- `http://localhost:8089/swagger-ui.html`

## Health checks

```powershell
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8087/actuator/health
curl http://localhost:8089/actuator/health
```

## Fluxos principais de API

### Autenticacao

- `POST /v1/public/auth/register` com `displayName`
- `POST /v1/public/auth/login`
- `POST /v1/public/auth/refresh`
- `GET /v1/auth/me`

### Trilhas e jornada

- `GET /v1/trails`
- `POST /v1/trails` admin
- `GET /v1/trails/journey/current`
- `POST /v1/trails/internal/journey/current`

### Check-in

- `GET /v1/check-ins`
- `POST /v1/check-ins`

### Reflexoes e espacos

- `GET /v1/posts`
- `POST /v1/posts`
- `GET /v1/communities`
- `POST /v1/communities`
- `POST /v1/communities/{id}/join`
- `POST /v1/communities/{id}/leave`

### Chat

- `GET /v1/messages`
- `POST /v1/messages`
- WebSocket STOMP: `ws://localhost:8086/ws/chat`

### Assinaturas

- `GET /v1/plans`
- `GET /v1/subscription/current`
- `GET /v1/subscriptions`
- `POST /v1/billing/checkout`
- `GET /v1/billing/checkout/{checkoutId}`
- `POST /v1/subscription/cancel`
- `POST /v1/public/billing/mercadopago/webhook`

### Perfil

- `GET /v1/profiles/me`
- `PUT /v1/profiles/me`
- `POST /v1/profiles/me/avatar`
- `GET /v1/public/profiles/avatar/{fileName}`

O perfil principal agora concentra `displayName`, `birthDate`, `gender`, `customGender`, `avatarUrl`, `bio` e `journeyLevel`.

### Notificacoes

- `GET /v1/notifications`
- `GET /v1/notifications/unread-count`
- `POST /v1/notifications/{id}/read`
- `POST /v1/notifications/read-all`
- `POST /v1/admin/notifications` admin

## Variaveis importantes

### Google login

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`
- `GOOGLE_ALLOWED_FRONTEND_ORIGIN_PATTERNS`

### IA

- `AI_PROVIDER`
- `AI_API_KEY`
- `AI_BASE_URL`
- `AI_MODEL`
- `AI_LANGUAGE`
- `AI_MAX_TOKENS`

### Billing

- `APP_BILLING_PROVIDER`
- `APP_BILLING_FRONTEND_BASE_URL`
- `APP_BILLING_PUBLIC_BASE_URL`
- `APP_BILLING_INTERNAL_TOKEN`
- `MERCADO_PAGO_BASE_URL`
- `MERCADO_PAGO_ACCESS_TOKEN`
- `MERCADO_PAGO_WEBHOOK_SECRET`

### Avatar e perfil

- `APP_PROFILE_AVATAR_STORAGE_PATH`

## Observacoes importantes

- O checkout premium so libera o acesso premium depois da confirmacao do backend.
- O `content-service` consulta o `subscription-service` internamente para validar acesso premium.
- Para producao com Mercado Pago, `APP_BILLING_PUBLIC_BASE_URL` precisa apontar para uma URL publica valida para webhook.
- O fluxo de Google Login ainda deve ser tratado como validacao final manual antes de go-live.
- O avatar do usuario e salvo pelo `user-service` em storage local persistido por volume Docker.

## Qualidade

Build:

```powershell
mvn -q -DskipTests package
```

Compilar modulos principais:

```powershell
mvn -pl services/auth-service,services/content-service,services/emotional-service,services/social-service,services/chat-service,services/subscription-service,services/notification-service,services/ai-service -am -DskipTests compile
```

Testes:

```powershell
mvn test
```

## Documentos complementares

- [manual-validation-matrix.md](./docs/manual-validation-matrix.md)
- [production-go-live.md](./docs/production-go-live.md)
- [cloud-azure-aws-free-tier.md](./docs/cloud-azure-aws-free-tier.md)
