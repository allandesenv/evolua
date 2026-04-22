# Go-Live de Producao

Guia unico para subir backend e frontend do Evolua em dois cenarios:

- `Docker VPS`
- `frontend separado`

## 1. Requisitos obrigatorios

### Dominio e URLs

- um dominio ou subdominio para o frontend
- uma URL publica para o backend/gateway
- uma URL publica para o webhook do Mercado Pago

### Segredos e variaveis

- `APP_SECURITY_JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`
- `AI_API_KEY`
- `AI_MODEL`
- `MERCADO_PAGO_ACCESS_TOKEN`
- `APP_BILLING_PUBLIC_BASE_URL`
- `APP_BILLING_FRONTEND_BASE_URL`
- `APP_BILLING_INTERNAL_TOKEN`
- `APP_PROFILE_AVATAR_STORAGE_PATH`

## 2. Cenario A: Docker VPS

### Recomendacao

- frontend servido por Nginx
- backend via `docker compose`
- proxy reverso apontando para gateway e frontend

### Passos

1. configurar DNS
2. subir banco/redis/mongo persistentes
3. preencher `.env` de producao
4. ajustar `docker-compose.yml` ou override de producao
5. subir com `docker compose up --build -d`
6. validar `docker compose ps`
7. validar health checks dos servicos
8. validar webhook publico do Mercado Pago
9. validar persistencia do volume de avatar do `user-service`

### Portas e proxy

- expor publicamente apenas frontend e gateway
- manter servicos internos atras do proxy
- usar TLS com certificado valido

## 3. Cenario B: Frontend separado

### Recomendacao

- gerar `flutter build web`
- hospedar em Nginx, CDN ou plataforma estatica
- backend continua separado em VPS ou cluster

### Passos

1. build:

```powershell
flutter build web `
  --dart-define=EVOLUA_API_BASE_URL=https://api.seudominio.com `
  --dart-define=EVOLUA_AUTH_BASE_URL=https://api.seudominio.com/auth-proxy `
  --dart-define=EVOLUA_USER_BASE_URL=https://api.seudominio.com/user-proxy `
  --dart-define=EVOLUA_CONTENT_BASE_URL=https://api.seudominio.com/content-proxy `
  --dart-define=EVOLUA_EMOTIONAL_BASE_URL=https://api.seudominio.com/emotional-proxy `
  --dart-define=EVOLUA_SOCIAL_BASE_URL=https://api.seudominio.com/social-proxy `
  --dart-define=EVOLUA_CHAT_BASE_URL=https://api.seudominio.com/chat-proxy `
  --dart-define=EVOLUA_SUBSCRIPTION_BASE_URL=https://api.seudominio.com/subscription-proxy `
  --dart-define=EVOLUA_NOTIFICATION_BASE_URL=https://api.seudominio.com/notification-proxy `
  --dart-define=EVOLUA_AI_BASE_URL=https://api.seudominio.com/ai-proxy
```

2. publicar o conteudo de `build/web`
3. garantir CORS coerente no backend
4. validar login, jornada, reflexoes, espacos, notificacoes e assinatura

## 4. Checklist de producao

- `docker compose ps` ou equivalente sem servico degradado
- Swagger/health acessiveis apenas onde fizer sentido
- secrets fora do repositório
- banco com backup
- logs centralizados
- Mercado Pago com callback publico funcional
- Google OAuth com origens e redirect finais configurados
- avatar do usuario persistindo entre restarts do `user-service`
- teste manual com admin e usuario comum
- teste de checkout real

## 5. Bloqueios externos conhecidos

- checkout premium nao fecha ponta a ponta sem `MERCADO_PAGO_ACCESS_TOKEN` valido e URL publica de webhook
- Google Login ainda precisa de validacao final no ambiente definitivo

## 6. Ultima validacao antes de liberar

- login por email admin e usuario comum
- check-in com retorno de IA
- abrir jornada atual
- publicar reflexao
- entrar/sair de espaco
- enviar notificacao manual como admin
- validar badge/inbox de notificacoes
- iniciar checkout premium

## 7. Guias de nuvem em nivel iniciante

- [cloud-azure-aws-free-tier.md](./cloud-azure-aws-free-tier.md)

## 8. Recomendacao pratica para o Evolua hoje

Para o estado atual do projeto, a melhor relacao entre simplicidade, custo e chance de sucesso e:

- frontend Flutter Web em hospedagem estatica
- backend inteiro em uma VM Linux com `docker compose`

Guia detalhado:

- Azure e AWS em nivel gratuito: [cloud-azure-aws-free-tier.md](./cloud-azure-aws-free-tier.md)
