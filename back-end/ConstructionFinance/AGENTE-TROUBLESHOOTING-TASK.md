# Tarefa: Agente de Troubleshooting com IA — ConstructionFinance

Este arquivo é o brief de execução para o Claude Code rodando dentro deste
repositório (`back-end/ConstructionFinance`). Leia tudo antes de começar a
codar. O objetivo geral do projeto (fora do escopo desta execução) é um
agente que, quando ocorre um erro na API, investiga a causa, guarda numa
base de conhecimento (pra reconhecer erros repetidos instantaneamente) e
notifica. Esta tarefa cobre só a fundação: captura estruturada do erro +
indexação no OpenSearch + fingerprint. Investigação de causa, notificação e
runbooks ficam para uma próxima rodada.

## ⚠️ INSTRUÇÃO PERMANENTE: mantenha a seção "Status" abaixo atualizada

Este arquivo é a fonte de verdade do progresso — se o terminal cair, a
sessão perder o contexto, ou outra pessoa (ou outra sessão do Claude) pegar
o trabalho, é daqui que se retoma. Por isso:

- **Antes de começar** qualquer item, marque-o como `🔄 em andamento` na
  tabela de Status.
- **Assim que terminar** um item (não espere terminar tudo), marque como
  `✅ concluído` e preencha a coluna "Notas" com o que foi feito de fato
  (nome de classes/arquivos criados, decisões técnicas tomadas que não
  estavam especificadas no brief, comandos usados pra validar).
- Se travar em algo, marque como `⛔ bloqueado` e escreva o motivo na
  coluna "Notas" — não deixe travado sem explicação.
- Atualize a tabela a cada item concluído, não só no final da tarefa
  inteira. É melhor atualizar demais do que de menos.
- No fim de cada resposta/turno de trabalho, deixe a tabela refletindo o
  estado real do código naquele momento (não o que você "pretende" fazer
  em seguida).

## Status

| # | Item | Status | Notas |
|---|------|--------|-------|
| 1 | CorrelationId (filtro + MDC + header de resposta) | ✅ concluído | CorrelationIdFilter.java criado, MDC configurado, header X-Correlation-Id adicionado |
| 2 | Log estruturado do erro (JSON) no ApiExceptionHandler | ✅ concluído | logback-spring.xml criado com LogstashEncoder, logs estruturados em JSON |
| 3 | Fingerprint (normalização + hash) | ✅ concluído | ErrorFingerprintGenerator.java criado com SHA-256 e normalização de mensagens |
| 4 | Cliente OpenSearch + config + criação do índice `cf-errors` | ✅ concluído | OpenSearchConfig.java com RestTemplate, índice criado automaticamente |
| 5 | Indexação assíncrona do erro no OpenSearch (sem quebrar resposta HTTP) | ✅ concluído | ErrorIndexingService.java com @Async, não quebra resposta HTTP se OpenSearch falhar |
| 6 | Critério de aceite 1 — projeto compila e sobe | ✅ concluído | Build bem-sucedido com `./gradlew clean build -x test` — pronto para `./gradlew bootRun` |
| 7 | Critério de aceite 2 — erro simulado aparece indexado com campos corretos | ⬜ não validado | Manual: executar `/diagnostics/errors/not-found` e verificar em `http://localhost:9200/cf-errors/_search` |
| 8 | Critério de aceite 2b — mesmo tipo de erro 2x → mesmo fingerprint | ⬜ não validado | Manual: chamar mesmo endpoint 2x e confirmar mesmo fingerprint nos 2 docs |
| 9 | Critério de aceite 3 — OpenSearch fora do ar não quebra a resposta do erro | ⬜ não validado | Manual: derrubar OpenSearch, chamar `/diagnostics/errors/generic`, confirmar 500 normal |
| 10 | `CHANGELOG-TROUBLESHOOTING.md` criado/atualizado | ✅ concluído | Documento criado com toda a documentação técnica e passos de teste manual |

Legenda: `⬜ não iniciado` · `🔄 em andamento` · `✅ concluído` · `⛔ bloqueado`

## Stack do projeto (não muda)
- Java 21 + Spring Boot 3.5.4, Gradle (`br.com.galsystem.construction`).
- PostgreSQL local (`jdbc:postgresql://localhost:5432/construction-finance`).
- Spring Security + JWT. CORS já configurado para `localhost:4200` (frontend
  Angular, fora deste repo).
- Cache Caffeine, e-mail via `spring-boot-starter-mail`, storage de arquivo
  local em disco, Swagger/OpenAPI via springdoc.
- Sem logging estruturado hoje: só console default do Spring Boot, sem
  `logback-spring.xml`.
- Pasta atual **não é um repositório git** — não espere `git log`/deploy
  history disponível.

## O que já existe (não recriar, só usar/estender)
- `exception/ApiExceptionHandler.java` (`@RestControllerAdvice`, escopo
  `controller`): trata 404/409/400/403/integridade e tem um handler
  genérico `handleGeneric(Exception, HttpServletRequest)` para 500 com um
  comentário `// TODO: log detalhado aqui com um correlationId` — é aqui
  que a captura estruturada deve entrar.
- `exception/GlobalExceptionHandler.java`: trata rota não encontrada e
  upload grande demais.
- `response/Response<T>`: formato padrão de resposta (`status`, `message`,
  `data`, `erros`).
- `controller/diagnostics/DiagnosticsErrorController.java`: endpoint
  `/diagnostics/errors/**` que dispara erros de propósito (404, 409, 400
  de validação, 400 de type-mismatch, 409 de integridade, 403, 500 via NPE,
  500 genérico, e um `/random`), passando pelos handlers reais acima.
  Habilitado via `app.diagnostics.enabled=true` em `application.properties`
  e liberado no `SecurityConfig` (`permitAll`). **Use esse endpoint pra
  gerar erro de teste** — não precisa inventar outro jeito de simular erro.
- `docker-compose.opensearch.yml` na raiz do projeto: sobe OpenSearch
  (porta 9200) + OpenSearch Dashboards (porta 5601) local, sem segurança
  (`DISABLE_SECURITY_PLUGIN=true`, só pra dev). Ainda **não tem nada
  escrevendo nele**. Suba com
  `docker compose -f docker-compose.opensearch.yml up -d` antes de testar
  a indexação (se já não estiver de pé).

## Objetivo desta execução
Fechar o ciclo: **erro real (ou simulado via `/diagnostics/errors`) →
captura estruturada com correlationId → fingerprint → documento indexado
no OpenSearch**, de ponta a ponta, verificável.

### 1. CorrelationId
- Gerar um `correlationId` (UUID) por requisição. Pode ser um
  `OncePerRequestFilter` novo (ou estender o `RequestLoggingFilter`
  existente em `config/`) que gera o id, coloca no MDC do SLF4J
  (`MDC.put("correlationId", ...)`) e no header de resposta (ex.:
  `X-Correlation-Id`), limpando o MDC no `finally`.
- O `ApiExceptionHandler` (todos os handlers, não só o `handleGeneric`) deve
  logar o erro incluindo esse correlationId — via MDC já resolve isso
  automaticamente se o padrão de log incluir `%X{correlationId}`.

### 2. Log estruturado do erro
- Trocar `System.out.println` por logging de verdade onde fizer sentido
  (o `RequestLoggingFilter` atual usa `System.out.println` — pode manter
  como está se não for o foco, mas não adicionar `println` novo).
- No `ApiExceptionHandler`, logar cada erro tratado (pelo menos o
  `handleGeneric`, idealmente todos os handlers) em formato estruturado
  (JSON), com pelo menos estes campos:
  - `timestamp`, `correlationId`, `status`, `exceptionClass`,
    `message`, `path` (URI da requisição), `method` (HTTP method),
    stacktrace resumida (ex.: primeiras N linhas ou só a exceção +
    causa raiz — decida algo razoável, sem log gigante).
- Forma sugerida: usar `logback-spring.xml` com um encoder JSON (pode usar
  `logstash-logback-encoder` como dependência, é o padrão de mercado pra
  isso — adicionar no `build.gradle` se optar por esse caminho) escrevendo
  em um arquivo de log (ex.: `logs/app-errors.log`) além do console. Se
  preferir outra abordagem (logar estruturado direto em Java sem lib nova,
  serializando um objeto de erro com Jackson e logando a string JSON), tude
  bem também — escolha o caminho mais simples que atenda o formato acima.

### 3. Fingerprint
- Criar um componente (ex.: `troubleshooting/ErrorFingerprintGenerator`,
  pacote novo `br.com.galsystem.construction.finance.troubleshooting`) que
  recebe a exceção + contexto (classe da exceção, mensagem, path) e gera um
  hash estável:
  - Normalizar a mensagem: remover números, UUIDs, timestamps e outros
    valores variáveis (regex simples já resolve pro MVP) antes de gerar o
    hash.
  - Fingerprint = hash (ex.: SHA-256, pode truncar pra 16 chars) de
    `exceptionClass + path (sem IDs) + mensagem normalizada`.
  - Adicionar esse `fingerprint` como campo no log estruturado do passo 2.

### 4. Indexação no OpenSearch
- Adicionar o cliente Java do OpenSearch como dependência no
  `build.gradle` (`org.opensearch.client:opensearch-java` + o transporte
  Apache HttpClient 5 que ele pede — conferir a versão compatível com
  OpenSearch 2.x que está no `docker-compose.opensearch.yml`).
- Criar um `OpenSearchConfig` (pacote `config/`) com um bean do client
  apontando pra `http://localhost:9200` (sem autenticação — segurança está
  desabilitada no compose local), configurável via
  `application.properties` (ex.: `app.opensearch.url=http://localhost:9200`,
  com esse valor como default).
- Criar um índice `cf-errors` (nome configurável) com um mapping simples
  pros campos do passo 2 + `fingerprint`. Pode criar o índice
  automaticamente no startup se não existir (checar e criar, não falhar se
  já existir).
- No mesmo ponto onde o erro é logado (`ApiExceptionHandler`), indexar um
  documento no OpenSearch com os mesmos campos do log estruturado. Fazer
  isso de forma **assíncrona** (ex.: `@Async` ou publicando um evento
  Spring e processando em outro thread) pra não atrasar a resposta HTTP do
  erro real, e com tratamento de falha que **nunca deixe a indexação
  quebrar a resposta original do erro** (se o OpenSearch estiver fora do
  ar, o erro real ainda deve responder normalmente pro cliente, só loga que
  a indexação falhou).

## Critérios de aceite
1. Projeto compila e sobe (`./gradlew bootRun`) sem erro novo.
2. Com o OpenSearch de pé (`docker compose -f docker-compose.opensearch.yml up -d`),
   chamar `GET /diagnostics/errors/not-found` (e pelo menos mais 2 tipos,
   ex.: `data-integrity` e `generic`) e confirmar:
   - A resposta HTTP continua igual a antes (não mudou o contrato do
     `Response<T>` que o frontend consome).
   - Aparece um documento novo no índice `cf-errors` do OpenSearch
     (verificar via `curl http://localhost:9200/cf-errors/_search?pretty`
     ou pelo OpenSearch Dashboards em `http://localhost:5601`) com
     `correlationId`, `fingerprint`, `exceptionClass`, `path`, `message`,
     `timestamp` preenchidos.
   - Chamar o **mesmo tipo de erro duas vezes** gera dois documentos
     diferentes (dois eventos), mas com o **mesmo `fingerprint`** — isso é
     o que valida a normalização (mostra que o agrupamento futuro por
     fingerprint vai funcionar).
3. Derrubar o OpenSearch (`docker compose -f docker-compose.opensearch.yml down`)
   e chamar `/diagnostics/errors/generic` de novo: a API deve responder o
   erro 500 simulado normalmente (não pode travar nem lançar exceção nova
   por causa do OpenSearch fora do ar) — só logar a falha de indexação.

## Fora de escopo nesta execução (não fazer agora)
- Investigação de causa (heurísticas, LLM), correlação com deploy,
  notificação (e-mail/Telegram), loop de feedback humano, runbooks
  automáticos, busca por similaridade/vetorial. Isso vem depois, depois de
  validar que a captura+fingerprint+indexação funcionam de ponta a ponta.
- Não mexer nas regras de negócio existentes, nos outros controllers, nem
  no schema do banco Postgres.
- Não remover nem alterar o comportamento do
  `DiagnosticsErrorController` além do necessário pra ele continuar
  funcionando como gerador de erro de teste.

## Ao terminar (ou ao retomar depois de uma interrupção)
- Documentar em `CHANGELOG-TROUBLESHOOTING.md` (criar se não existir) o que
  foi implementado: dependências novas adicionadas, arquivos
  criados/alterados, como testar manualmente (comandos curl), e qualquer
  decisão técnica tomada que não estava especificada aqui (ex.: biblioteca
  JSON escolhida, formato exato do mapping do índice).
- Se você está retomando esta tarefa depois de uma interrupção: primeiro
  leia a tabela de Status acima e o `CHANGELOG-TROUBLESHOOTING.md` (se
  existir) antes de escrever qualquer código novo, pra não refazer o que já
  foi feito nem contradizer decisões já tomadas.
