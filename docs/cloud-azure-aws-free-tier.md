# Evolua na Azure e na AWS com foco em nivel gratuito

Guia realista, em nivel iniciante, para subir o Evolua com a arquitetura que o projeto realmente tem hoje.

Este passo a passo parte do estado atual do repositorio:

- frontend Flutter Web em `evolua-frontend`
- backend em microsservicos com `docker compose`
- 10 servicos Java
- 5 bancos PostgreSQL
- 2 bancos MongoDB
- 1 Redis

Por isso, a forma mais barata e mais coerente para producao inicial nao e "um App Service + um PostgreSQL". Para o Evolua real, o melhor ponto de partida e:

- frontend em hospedagem estatica
- backend inteiro em uma VM Linux
- `docker compose` rodando servicos e bancos na mesma maquina

Esse desenho funciona tanto na Azure quanto na AWS e reduz muito atrito para a primeira subida.

## 1. O que o projeto realmente sobe hoje

Do `docker-compose.yml` atual:

- `gateway-service`
- `auth-service`
- `user-service`
- `content-service`
- `emotional-service`
- `ai-service`
- `social-service`
- `chat-service`
- `subscription-service`
- `notification-service`
- `postgres-auth`
- `postgres-user`
- `postgres-content`
- `postgres-emotional`
- `postgres-subscription`
- `mongo-social`
- `mongo-chat`
- `redis`

## 2. Decisao recomendada

### Azure

- Frontend: `Azure Static Web Apps` Free
- Backend: `Azure Virtual Machine` Linux
- Dados: no proprio `docker compose` da VM

### AWS

- Frontend: `AWS Amplify Hosting`
- Backend: `Amazon EC2` Linux
- Dados: no proprio `docker compose` da instancia

## 3. Por que nao usar App Service unico ou banco gerenciado agora

No estado atual do Evolua:

- um unico `App Service` nao cobre o backend inteiro
- um App Service por microservico foge rapido do nivel gratuito
- so `PostgreSQL gerenciado` nao resolve `MongoDB + Redis`
- usar varios servicos gerenciados agora aumenta custo, configuracao e pontos de falha

Entao, para aprender cloud, subir rapido e segurar custo, a boa pratica inicial aqui e:

- frontend separado e estatico
- uma VM pequena para backend
- proxy reverso
- segredos por ambiente

Depois, com uso real, voce pode migrar por etapas para banco gerenciado, Redis gerenciado, monitoramento e CI/CD.

## 3.1 Volume persistente de avatar

Com avatar real no perfil, o `user-service` passa a precisar de storage persistente para imagens.

No `docker compose`, preserve um volume dedicado para esse caminho, por exemplo:

- `user-profile-avatars:/data/avatars`

Se a VM for recriada sem esse volume, os avatares enviados se perdem.

## 4. Variaveis reais do projeto

O backend usa hoje, no minimo:

- `APP_SECURITY_JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`
- `GOOGLE_ALLOWED_FRONTEND_ORIGIN_PATTERNS`
- `AI_PROVIDER`
- `AI_API_KEY`
- `AI_BASE_URL`
- `AI_MODEL`
- `AI_LANGUAGE`
- `AI_MAX_TOKENS`
- `AI_TEMPERATURE`
- `AI_TIMEOUT_SECONDS`
- `AI_FALLBACK_ENABLED`
- `APP_BILLING_PROVIDER`
- `APP_BILLING_FRONTEND_BASE_URL`
- `APP_BILLING_PUBLIC_BASE_URL`
- `APP_BILLING_INTERNAL_TOKEN`
- `APP_PROFILE_AVATAR_STORAGE_PATH`
- `MERCADO_PAGO_BASE_URL`
- `MERCADO_PAGO_ACCESS_TOKEN`
- `MERCADO_PAGO_WEBHOOK_SECRET`

O frontend usa hoje estes `dart-define`:

- `EVOLUA_API_BASE_URL`
- `EVOLUA_AUTH_BASE_URL`
- `EVOLUA_USER_BASE_URL`
- `EVOLUA_CONTENT_BASE_URL`
- `EVOLUA_EMOTIONAL_BASE_URL`
- `EVOLUA_SOCIAL_BASE_URL`
- `EVOLUA_CHAT_BASE_URL`
- `EVOLUA_SUBSCRIPTION_BASE_URL`
- `EVOLUA_NOTIFICATION_BASE_URL`
- `EVOLUA_AI_BASE_URL`

## 5. Antes de subir: ajuste obrigatorio de segredos

O `.env` local atual foi usado para desenvolvimento e contem segredos sensiveis. Antes de producao:

1. rotacione `GOOGLE_CLIENT_SECRET`
2. rotacione `AI_API_KEY`
3. gere um `APP_SECURITY_JWT_SECRET` forte e novo
4. gere um `APP_BILLING_INTERNAL_TOKEN` forte e novo
5. nunca reutilize o `.env` local exatamente como esta em producao

## 6. Azure passo a passo

As recomendacoes abaixo seguem a direcao das ofertas oficiais da Azure para conta gratuita, `Static Web Apps` e VMs Linux. Como limites e creditos podem mudar, confirme no portal da sua conta antes de provisionar.

### 6.1 Arquitetura recomendada na Azure

- `app.seu-dominio.com` -> frontend Flutter Web no `Azure Static Web Apps`
- `api.seu-dominio.com` -> Nginx na VM apontando para o `gateway-service`
- backend, bancos e Redis rodando por `docker compose` em uma VM Linux

### 6.2 Criar a base da conta

1. Crie a conta gratuita da Azure.
2. Crie um `Resource Group`.

Sugestao:

- nome: `evolua-rg`
- regiao: `Brazil South`

### 6.3 Criar a VM do backend

Crie uma `Virtual Machine` Linux.

Configuracao recomendada para comecar:

- imagem: `Ubuntu Server LTS`
- tamanho: o menor burstable compativel com a oferta da conta, como `B1s` se elegivel
- autenticacao: chave SSH
- IP publico: sim
- portas liberadas: `22`, `80`, `443`

Nao exponha publicamente:

- `8081` ate `8089`
- `5432`
- `27017`
- `6379`

### 6.4 Preparar a VM

Conecte por SSH e rode:

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y docker.io docker-compose-plugin nginx certbot python3-certbot-nginx git
sudo usermod -aG docker $USER
```

Saia e entre novamente na sessao SSH.

### 6.5 Clonar o backend e preparar `.env`

Na VM:

```bash
git clone <URL_DO_REPOSITORIO_BACKEND>
cd evolua
cp .env .env.production
```

Preencha o `.env.production` com os valores de producao e depois exporte ou referencie esse arquivo no deploy.

Minimo obrigatorio:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI=https://app.seu-dominio.com/auth/google/callback`
- `GOOGLE_ALLOWED_FRONTEND_ORIGIN_PATTERNS=https://app.seu-dominio.com`
- `AI_PROVIDER=openai`
- `AI_API_KEY=<sua-chave>`
- `AI_BASE_URL=https://api.openai.com/v1`
- `AI_MODEL=gpt-5.4-mini`
- `APP_BILLING_PROVIDER=mercadopago`
- `APP_BILLING_FRONTEND_BASE_URL=https://app.seu-dominio.com`
- `APP_BILLING_PUBLIC_BASE_URL=https://api.seu-dominio.com`
- `APP_BILLING_INTERNAL_TOKEN=<token-forte>`
- `MERCADO_PAGO_BASE_URL=https://api.mercadopago.com`
- `MERCADO_PAGO_ACCESS_TOKEN=<token-real>`
- `MERCADO_PAGO_WEBHOOK_SECRET=<segredo-se-usar>`

Importante:

- o webhook do Mercado Pago precisa de URL publica real
- o Google Login precisa que o redirect configurado no Google Cloud Console bata exatamente com a URL final

### 6.6 Subir o backend real

No servidor:

```bash
docker compose --env-file .env.production up --build -d
docker compose ps
```

### 6.7 Configurar o proxy reverso

O desenho recomendado e:

- `api.seu-dominio.com` -> `localhost:8080`

Exemplo basico de Nginx:

```nginx
server {
    server_name api.seu-dominio.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Depois:

```bash
sudo nginx -t
sudo systemctl reload nginx
sudo certbot --nginx -d api.seu-dominio.com
```

### 6.8 Subir o frontend no Azure Static Web Apps

No projeto do frontend:

```powershell
flutter build web `
  --dart-define=EVOLUA_API_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_AUTH_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_USER_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_CONTENT_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_EMOTIONAL_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_SOCIAL_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_CHAT_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_SUBSCRIPTION_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_NOTIFICATION_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_AI_BASE_URL=https://api.seu-dominio.com
```

Depois crie o `Static Web App`:

- nome sugerido: `evolua-frontend`
- plano: `Free`
- origem: GitHub ou deploy do build estatico

Se usar dominio proprio:

- `app.seu-dominio.com` -> Azure Static Web Apps

### 6.9 Checklist de validacao na Azure

- `https://api.seu-dominio.com/actuator/health` responde no gateway
- login por email funciona
- check-in salva e devolve insight
- jornada atual abre
- reflexoes e espacos funcionam
- notificacoes no sino aparecem
- assinatura carrega planos e status
- Google Login usa a origem final correta
- Mercado Pago aponta webhook para a URL publica real

### 6.10 O que pode sair do "quase gratis"

- VM fora da oferta gratuita ou acima dos creditos
- trafego alto
- dominio proprio
- armazenamento adicional
- uso continuo apos o periodo promocional

## 7. AWS passo a passo

As recomendacoes abaixo seguem a direcao do free tier oficial da AWS para EC2 e hospedagem web. Como elegibilidade, creditos e limites podem mudar, confira o painel da sua conta antes do deploy.

### 7.1 Arquitetura recomendada na AWS

- `app.seu-dominio.com` -> frontend no `AWS Amplify Hosting`
- `api.seu-dominio.com` -> Nginx na EC2 apontando para o gateway
- backend, bancos e Redis rodando por `docker compose` na EC2

### 7.2 Criar a conta e escolher a regiao

1. Crie a conta AWS.
2. Escolha a regiao.

Sugestao:

- `sa-east-1` se quiser Brasil

Se o tipo elegivel ao free tier nao estiver bom na regiao escolhida, compare com outra regiao antes de seguir.

### 7.3 Criar a EC2 do backend

Crie uma instancia Linux:

- imagem: `Ubuntu Server LTS` ou `Amazon Linux`
- tipo: `t3.micro` ou `t2.micro`, conforme elegibilidade da conta
- autenticacao: chave SSH

Security Group:

- `22` para SSH
- `80` para HTTP
- `443` para HTTPS

De novo: nao exponha portas internas dos microsservicos.

### 7.4 Preparar a instancia

Se usar Ubuntu:

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y docker.io docker-compose-plugin nginx certbot python3-certbot-nginx git
sudo usermod -aG docker $USER
```

Se usar Amazon Linux, adapte a instalacao de Docker, Nginx e Certbot conforme o SO.

### 7.5 Clonar backend e configurar `.env`

Na EC2:

```bash
git clone <URL_DO_REPOSITORIO_BACKEND>
cd evolua
cp .env .env.production
```

Preencha as mesmas variaveis de producao listadas na secao Azure, trocando apenas as URLs finais:

- `GOOGLE_REDIRECT_URI=https://app.seu-dominio.com/auth/google/callback`
- `GOOGLE_ALLOWED_FRONTEND_ORIGIN_PATTERNS=https://app.seu-dominio.com`
- `APP_BILLING_FRONTEND_BASE_URL=https://app.seu-dominio.com`
- `APP_BILLING_PUBLIC_BASE_URL=https://api.seu-dominio.com`

### 7.6 Subir o backend

```bash
docker compose --env-file .env.production up --build -d
docker compose ps
```

### 7.7 Configurar Nginx e HTTPS

Mesmo principio da Azure:

- `api.seu-dominio.com` -> `127.0.0.1:8080`

Depois:

```bash
sudo nginx -t
sudo systemctl reload nginx
sudo certbot --nginx -d api.seu-dominio.com
```

### 7.8 Subir o frontend na AWS

Opcao mais amigavel para comecar: `AWS Amplify Hosting`.

Build local:

```powershell
flutter build web `
  --dart-define=EVOLUA_API_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_AUTH_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_USER_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_CONTENT_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_EMOTIONAL_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_SOCIAL_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_CHAT_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_SUBSCRIPTION_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_NOTIFICATION_BASE_URL=https://api.seu-dominio.com `
  --dart-define=EVOLUA_AI_BASE_URL=https://api.seu-dominio.com
```

Depois:

- crie um app no `AWS Amplify`
- conecte ao repositorio ou publique o build estatico
- aponte `app.seu-dominio.com` para o frontend

Se quiser um caminho ainda mais manual, pode usar `S3 + CloudFront`, mas `Amplify` costuma ser mais amigavel para primeira subida.

### 7.9 Checklist de validacao na AWS

- `https://api.seu-dominio.com/actuator/health` responde
- frontend abre em `https://app.seu-dominio.com`
- login por email funciona
- trilhas, check-in, reflexoes, espacos e perfil funcionam
- notificacoes no sino carregam
- assinatura mostra status real
- Mercado Pago consegue chamar a URL publica do webhook

## 8. O que muda no projeto para producao real

### Backend

- troque todos os segredos de desenvolvimento
- use `.env.production`
- mantenha apenas `80/443` publicos
- exponha o sistema ao mundo pelo gateway
- monitore consumo de disco, memoria e CPU da VM

### Frontend

- gere o build sempre com `--dart-define` apontando para a URL publica final
- valide o fluxo de login Google com a origem final
- valide o retorno do billing com a URL final do frontend

## 9. Boas praticas minimas antes de liberar

- backup dos volumes Docker
- rotina de restart automatica dos containers
- logs do Nginx e `docker compose logs`
- renovacao automatica de certificado
- validacao manual com admin e usuario comum
- smoke dos fluxos principais apos cada deploy

## 10. O que eu recomendo para o Evolua hoje

Se a meta e subir rapido, barato e com boa chance de dar certo:

### Melhor opcao na Azure

- frontend no `Azure Static Web Apps`
- backend inteiro em `1 VM Linux`

### Melhor opcao na AWS

- frontend no `AWS Amplify`
- backend inteiro em `1 EC2`

## 11. Limites e bloqueios conhecidos

- `MERCADO_PAGO_ACCESS_TOKEN` ainda precisa estar preenchido para checkout real
- `APP_BILLING_PUBLIC_BASE_URL` precisa ser publico e valido para webhook
- Google Login deve ser validado no dominio final antes de producao
- uma VM pequena pode ficar justa para o conjunto completo de microsservicos; monitore uso e escale se necessario

## 12. Referencias oficiais

Este guia foi alinhado com a documentacao oficial da Azure e da AWS para conta gratuita, VMs Linux, hospedagem estatica e ofertas iniciais. Como esses programas mudam com o tempo, confira no dia do deploy:

- Azure Free Account
- Azure Static Web Apps
- Azure Virtual Machines
- AWS Free Tier
- Amazon EC2 Free Tier
- AWS Amplify Hosting
