# Validacao Manual Core

Data de referencia: 2026-04-02

| Fluxo | Perfil | Resultado esperado | Resultado obtido | Status | Bloqueios |
| --- | --- | --- | --- | --- | --- |
| Login por email | Usuario comum | Entrar e abrir `/home` com sessao valida | Pendente de clique manual | Pendente | Nenhum bloqueio tecnico identificado nesta rodada |
| Login por email | Admin | Entrar e abrir `/home` com sessao valida e recursos administrativos | Pendente de clique manual | Pendente | Nenhum bloqueio tecnico identificado nesta rodada |
| Login por Google | Usuario comum | Entrar e abrir `/home` com sessao valida | Fluxo ainda pode retornar para a tela de login apos callback | Bloqueado conhecido | Revisao posterior do fluxo de sessao apos callback |
| Check-in sem texto livre | Usuario comum | Salvar check-in, receber insight cauteloso e trilha sugerida | Contrato e UI preparados; falta clique manual | Pendente | Nenhum bloqueio tecnico identificado nesta rodada |
| Check-in com texto livre | Usuario comum | Salvar check-in, receber insight contextualizado e trilha sugerida | Contrato e UI preparados; falta clique manual | Pendente | Nenhum bloqueio tecnico identificado nesta rodada |
| Abrir trilha sugerida | Usuario comum | Ir para Trilhas e abrir detalhe real da trilha | CTA aponta para modulo de trilhas; falta clique manual | Pendente | Nenhum bloqueio tecnico identificado nesta rodada |
| Trilhas listar e filtrar | Usuario comum | Listar trilhas, filtrar e abrir detalhe | Fluxo compilando e analisado; falta clique manual | Pendente | Nenhum bloqueio tecnico identificado nesta rodada |
| Criar trilha | Admin | Criar trilha e atualizar listagem | Fluxo compilando e analisado; falta clique manual | Pendente | Necessita sessao admin valida |
| Feed listar e publicar | Usuario comum | Listar posts, filtrar e publicar em comunidade ingressada | Fluxo compilando e analisado; falta clique manual | Pendente | Requer comunidade ingressada |
| Comunidade listar, criar, entrar e sair | Usuario comum | Explorar, criar, entrar e sair de comunidades | Fluxo compilando e analisado; falta clique manual | Pendente | Nenhum bloqueio tecnico identificado nesta rodada |
| Chat listar e enviar | Usuario comum | Enviar mensagem HTTP e listar historico | Fluxo compilando e analisado; falta clique manual | Pendente | WebSocket em tempo real deve ser verificado separadamente |
| Perfil criar e atualizar listagem | Usuario comum | Criar perfil e ver lista atualizada | Fluxo compilando e analisado; falta clique manual | Pendente | Nenhum bloqueio tecnico identificado nesta rodada |

## Checagens automaticas concluidas

- `flutter analyze`
- `flutter test`
- `mvn -pl services/ai-service,services/emotional-service -am -DskipTests compile`
- `mvn -pl services/auth-service,services/user-service,services/content-service,services/social-service,services/chat-service -am -DskipTests compile`
