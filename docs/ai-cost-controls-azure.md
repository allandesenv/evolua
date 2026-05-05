# Controles Operacionais de Custo de IA

Este documento registra os controles externos ao codigo do Evolua para reduzir risco de custo em Azure/OpenAI.

## Controles ja implementados no app

- `subscription-service` controla quota diaria de IA por usuario.
- Plano Free: 1 acao de IA por dia, com ate 1 credito extra diario por rewarded ad validado via callback server-side.
- Plano Premium: 10 acoes de IA por dia e sem anuncios.
- `ai-service` consulta quota antes de chamar OpenAI.
- Se a quota acabar, o app salva o check-in e retorna fallback rule-based sem chamada externa.
- Historico Free no `emotional-service` fica limitado aos ultimos 30 dias.

## Azure Cost Management

Configurar um Budget mensal para o resource group/projeto do Evolua:

- Budget inicial sugerido para MVP: definir conforme caixa disponivel e margem de teste.
- Criar alertas em 50%, 80% e 100% do budget.
- Direcionar alertas para email operacional e canal de monitoramento usado pelo time.
- Revisar custos de OpenAI, App Service, banco e trafego semanalmente durante o MVP.

## Azure OpenAI

Aplicar limites operacionais por deployment/modelo:

- Usar modelos baratos para respostas curtas do Free.
- Manter `max_output_tokens` reduzido para Free.
- Evitar enviar historico bruto; preferir resumo curto de contexto.
- Monitorar requests por minuto, tokens por minuto, latencia e taxa de erro.
- Separar deployment de producao e homologacao quando possivel.

## Azure API Management

Quando o gateway de IA passar pelo APIM, aplicar politicas por chave/usuario/plano:

- Rate limit por usuario autenticado.
- Quota por periodo para endpoints que disparam IA.
- Limite de tokens quando a politica estiver disponivel para o recurso usado.
- Retorno padronizado para estouro de quota, sem tentar chamada externa.

## Operacao

- Nao confiar em flags do cliente para liberar IA.
- Validar rewarded ads apenas por callback server-side do provedor.
- Registrar metricas de `quotaLimited`, `rewardedAdAvailable` e consumo por plano.
- Auditar sessoes de reward repetidas ou expiradas.
- Rotacionar `APP_BILLING_INTERNAL_TOKEN` fora do repositorio e manter segredo por ambiente.

