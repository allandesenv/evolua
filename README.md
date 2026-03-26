# Evolua Backend

Backend em microsservicos para o Evolua com Java 21, Spring Boot 3+, Maven, Docker Compose, OpenAPI e Clean Architecture.

## Arquitetura

- `gateway-service`: roteamento central da API
- `services/auth-service`: cadastro, login, refresh token e `me`
- `services/user-service`: perfis do usuario
- `services/content-service`: trilhas e conteudos
- `services/emotional-service`: check-ins emocionais
- `services/social-service`: posts e feed
- `services/chat-service`: mensagens e WebSocket
- `services/subscription-service`: assinatura premium
- `services/notification-service`: notificacoes baseadas em Redis

## Stack

- Java 21
- Maven 3.9+
- Spring Boot 3+
- PostgreSQL
- MongoDB
- Redis
- Docker e Docker Compose

## Requisitos locais

- Docker Desktop em execucao
- Java 21 no `PATH`
- Maven 3.9+ no `PATH`

## Estrutura do repositorio

```text
.
├── gateway-service
├── services
│   ├── auth-service
│   ├── user-service
│   ├── content-service
│   ├── emotional-service
│   ├── social-service
│   ├── chat-service
│   ├── subscription-service
│   └── notification-service
├── docker-compose.yml
└── pom.xml
```

## Build local

Build completo sem testes:

```bash
mvn -q -DskipTests package
```

Build de um modulo especifico:

```bash
mvn -q -pl services/auth-service -am -DskipTests package
```

## Subida com Docker

Subir tudo em background:

```bash
docker compose up --build -d
```

Ver containers:

```bash
docker compose ps
```

Logs consolidados:

```bash
docker compose logs -f
```

Parar ambiente:

```bash
docker compose down
```

Parar e remover volumes:

```bash
docker compose down -v
```

## Portas

- `8080`: gateway
- `8081`: auth-service
- `8082`: user-service
- `8083`: content-service
- `8084`: emotional-service
- `8085`: social-service
- `8086`: chat-service
- `8087`: subscription-service
- `8088`: notification-service

Infra:

- PostgreSQL auth: container interno `5432`
- PostgreSQL user: container interno `5432`
- PostgreSQL content: container interno `5432`
- PostgreSQL emotional: container interno `5432`
- PostgreSQL subscription: container interno `5432`
- Mongo social: container interno `27017`
- Mongo chat: container interno `27017`
- Redis: container interno `6379`

## Swagger

- Gateway: `http://localhost:8080/swagger-ui.html`
- Auth: `http://localhost:8081/swagger-ui.html`
- User: `http://localhost:8082/swagger-ui.html`
- Content: `http://localhost:8083/swagger-ui.html`
- Emotional: `http://localhost:8084/swagger-ui.html`
- Social: `http://localhost:8085/swagger-ui.html`
- Chat: `http://localhost:8086/swagger-ui.html`
- Subscription: `http://localhost:8087/swagger-ui.html`
- Notification: `http://localhost:8088/swagger-ui.html`

## Health checks

Exemplos:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

## Fluxo basico de teste manual

### 1. Registrar usuario

```bash
curl -X POST http://localhost:8081/v1/public/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"teste@evolua.local\",\"password\":\"123456\"}"
```

### 2. Fazer login

```bash
curl -X POST http://localhost:8081/v1/public/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"teste@evolua.local\",\"password\":\"123456\"}"
```

Copie o `accessToken` retornado.

### 3. Criar perfil

```bash
curl -X POST http://localhost:8082/v1/profiles ^
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"displayName\":\"Teste\",\"bio\":\"Perfil inicial\",\"journeyLevel\":1,\"premium\":false}"
```

### 4. Listar perfil

```bash
curl http://localhost:8082/v1/profiles ^
  -H "Authorization: Bearer SEU_ACCESS_TOKEN"
```

### 5. Criar trilha

```bash
curl -X POST http://localhost:8083/v1/trails ^
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Respiracao\",\"description\":\"Trilha inicial\",\"category\":\"ansiedade\",\"premium\":false}"
```

### 6. Criar check-in emocional

```bash
curl -X POST http://localhost:8084/v1/check-ins ^
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"mood\":\"calmo\",\"reflection\":\"Dia produtivo\",\"energyLevel\":7,\"recommendedPractice\":\"respiracao guiada\"}"
```

### 7. Criar post

```bash
curl -X POST http://localhost:8085/v1/posts ^
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"content\":\"Primeiro post\",\"community\":\"geral\",\"visibility\":\"PUBLIC\"}"
```

### 8. Criar mensagem

```bash
curl -X POST http://localhost:8086/v1/messages ^
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"recipientId\":\"user-2\",\"content\":\"Ola\"}"
```

### 9. Criar assinatura

```bash
curl -X POST http://localhost:8087/v1/subscriptions ^
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"planCode\":\"premium-monthly\",\"status\":\"ACTIVE\",\"billingCycle\":\"MONTHLY\",\"premium\":true}"
```

### 10. Criar notificacao

```bash
curl -X POST http://localhost:8088/v1/notifications ^
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"channel\":\"EMAIL\",\"message\":\"Lembrete diario\"}"
```

## WebSocket de chat

Endpoint STOMP:

```text
ws://localhost:8086/ws/chat
```

Destino para envio:

```text
/app/chat.send
```

Topico de recebimento:

```text
/topic/chat/{recipientId}
```

Payload esperado:

```json
{
  "senderId": "user-1",
  "recipientId": "user-2",
  "content": "Ola"
}
```

## Testes automatizados

Executar todos:

```bash
mvn test
```

Executar um modulo:

```bash
mvn -pl services/auth-service test
```

## Troubleshooting

Imagem Docker nao encontrada:

- confirme que os `Dockerfile`s usam `maven:3.9.9-eclipse-temurin-21`
- confirme que os `Dockerfile`s usam `eclipse-temurin:21-jre`

Porta ocupada:

- pare containers antigos com `docker compose down`
- revise processos locais usando as portas `8080` a `8088`

Falha de autenticacao:

- gere um novo token via `/v1/public/auth/login`
- envie `Authorization: Bearer <token>`

Erro de banco:

- recrie o ambiente com `docker compose down -v`
- suba novamente com `docker compose up --build -d`

## Observacoes

- O projeto esta padronizado para Java 21.
- O gateway apenas roteia as chamadas para os microsservicos internos.
- Os serviços expõem Swagger individualmente para facilitar desenvolvimento e validação.
