# Validacao Manual Core

Data de referencia: 2026-04-14

| Fluxo | Perfil | Resultado esperado | Resultado obtido | Status | Bloqueios |
| --- | --- | --- | --- | --- | --- |
| Login por email | Usuario comum | Entrar e abrir `/home` com sessao valida | Validado por smoke tecnico; falta clique final humano | Pronto para validacao manual | Nenhum |
| Login por email | Admin | Entrar e abrir `/home` com sessao valida e recursos administrativos | Validado por smoke tecnico; falta clique final humano | Pronto para validacao manual | Nenhum |
| Login por Google | Usuario comum | Entrar e abrir `/home` com sessao valida | Continua exigindo validacao manual final em ambiente com callback publico confiavel | Bloqueio conhecido | Fluxo historicamente instavel no redirect final |
| Check-in sem texto livre | Usuario comum | Salvar check-in, receber insight e atualizar jornada | Contrato validado e compilado | Pronto para validacao manual | Nenhum |
| Check-in com texto livre | Usuario comum | Salvar check-in, receber resposta mais contextualizada e jornada | Contrato validado e compilado | Pronto para validacao manual | Nenhum |
| Trilhas listar e abrir | Usuario comum | Ver trilhas essenciais e bloqueio premium coerente | Gate premium e listagem compilados e testados | Pronto para validacao manual | Nenhum |
| Criar trilha | Admin | Criar trilha e atualizar listagem | Fluxo admin preparado | Pronto para validacao manual | Requer sessao admin |
| Reflexoes listar e publicar | Usuario comum | Publicar e listar reflexoes reais | Smoke de endpoints possivel; falta clique final | Pronto para validacao manual | Requer espaco ingressado para composer |
| Espacos listar, criar, entrar e sair | Usuario comum/admin | Explorar, entrar, sair e criar quando permitido | Fluxo compilado e smoke tecnico previsto | Pronto para validacao manual | Criacao depende do perfil com permissao |
| Chat listar e enviar | Usuario comum | Enviar mensagem HTTP e listar historico | Fluxo HTTP disponivel | Pronto para validacao manual | WebSocket exige duplo navegador na validacao final |
| Assinatura listar e consultar plano atual | Usuario comum | Ver plano atual e status de checkout | Fluxo novo de assinatura compilado | Pronto para validacao manual | Checkout real depende de credencial/token do Mercado Pago |
| Notificacoes inbox | Usuario comum | Ver badge, lista e marcar como lida | Backend e frontend alinhados | Pronto para validacao manual | Nenhum |
| Console admin de notificacoes | Admin | Enviar notificacao manual para usuario | Backend e frontend alinhados | Pronto para validacao manual | Requer sessao admin |

## Checagens automaticas desta rodada

- `docker compose ps`
- `flutter analyze`
- `flutter test`
- `mvn -pl services/auth-service,services/user-service,services/content-service,services/emotional-service,services/social-service,services/chat-service,services/subscription-service,services/notification-service,services/ai-service -am -DskipTests compile`

## Bloqueios externos reais

- webhook publico do Mercado Pago para confirmacao real do checkout
- validacao manual final do Google Login antes de go-live
