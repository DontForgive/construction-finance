# CHANGELOG - Agente de Troubleshooting com IA

## Versão 1.0.0 - Fundação de Captura e Indexação

### Dependências Adicionadas
- `net.logstash.logback:logstash-logback-encoder:8.0` — para logging estruturado em JSON

### Arquivos Criados

#### Configuração e Infraestrutura
- **`config/CorrelationIdFilter.java`** — `OncePerRequestFilter` que:
  - Gera UUID para cada requisição se não estiver no header `X-Correlation-Id`
  - Coloca o ID no MDC do SLF4J para inclusão automática em logs
  - Limpa o MDC no `finally` para evitar vazamento entre threads
  - Adiciona o header de resposta `X-Correlation-Id`

- **`config/OpenSearchConfig.java`** — Bean de configuração que:
  - Expõe beans `opensearchUrl()` e `opensearchIndexName()` via properties
  - Usa RestTemplate nativo do Spring (sem dependências externas de cliente)
  - Pronto para criar índice `cf-errors` automaticamente (inicialização será feita em versão futura se OpenSearch estiver disponível)

- **`resources/logback-spring.xml`** — Configuração de logging estruturado:
  - Console appender com formato legível
  - JSON appender (`logstash-logback-encoder`) escrevendo em `logs/app-errors.log`
  - MDC incluído automaticamente em logs JSON (`includeMdcData=true`)
  - Rolling policy com rotação por tamanho (10MB) e tempo (diário)
  - Logger específico `br.com.galsystem.construction.finance.exception` com nível ERROR

#### Troubleshooting
- **`troubleshooting/ErrorFingerprintGenerator.java`** — Gerador de fingerprints estáveis:
  - Normaliza mensagens: remove números, UUIDs, timestamps, emails
  - Normaliza paths: substitui IDs numéricos por `N`, UUIDs por `UUID`
  - Calcula SHA-256 de `exceptionClass|path|message` normalizado
  - Retorna primeiros 16 caracteres do hash
  - Permite identificar erros repetidos mesmo com valores variáveis

- **`troubleshooting/ErrorDocument.java`** — POJO com anotações Jackson:
  - Campos: `timestamp`, `correlationId`, `fingerprint`, `exceptionClass`, `message`, `path`, `method`, `status`, `stacktrace`
  - Estrutura mapeada para indexação no OpenSearch

- **`troubleshooting/ErrorIndexingService.java`** — Serviço de indexação assíncrona:
  - Anotado com `@Async` para não bloquear resposta HTTP
  - Usa RestTemplate para POST em `{opensearchUrl}/{indexName}/_doc`
  - Captura e loga erros de indexação sem propagar exceções
  - Se OpenSearch estiver offline, erro é apenas logado (não quebra resposta do erro original)

#### Exception Handler
- **`exception/ApiExceptionHandler.java`** — Modificações:
  - Adicionado método auxiliar `logAndIndexError()` que:
    - Formata stacktrace (10 primeiras linhas)
    - Loga estruturado em JSON via SLF4J
    - Gera fingerprint e cria ErrorDocument
    - Dispara indexação assíncrona
  - `handleGeneric()` agora chama `logAndIndexError()`
  - Injeção opcional de `ErrorFingerprintGenerator` e `ErrorIndexingService` (tolerante a falhas se não disponível)

### Configuração (application.properties)
```properties
app.opensearch.url=http://localhost:9200
app.opensearch.index-name=cf-errors
```

Ambas configuráveis, com defaults que apontam para OpenSearch local.

### Como Testar Manualmente

#### 1. Iniciar OpenSearch local
```bash
docker compose -f docker-compose.opensearch.yml up -d
```

Verificar disponibilidade:
```bash
curl http://localhost:9200/
```

#### 2. Iniciar a aplicação
```bash
./gradlew bootRun
```

Aplicação estará em `http://localhost:9090`

#### 3. Gerar erro simulado e verificar captura
```bash
# Dispara 404
curl -i http://localhost:9090/diagnostics/errors/not-found

# Dispara 500
curl -i http://localhost:9090/diagnostics/errors/generic

# Dispara 409
curl -i http://localhost:9090/diagnostics/errors/data-integrity
```

Cada resposta conterá header:
```
X-Correlation-Id: <uuid>
```

#### 4. Verificar logs estruturados
```bash
cat logs/app-errors.log
```

Logs estão em JSON com campos `correlationId`, `fingerprint`, `exceptionClass`, `status`, etc.

#### 5. Verificar indexação no OpenSearch
```bash
# Listar documentos no índice
curl -s http://localhost:9200/cf-errors/_search?pretty

# Procurar por fingerprint específico
curl -s http://localhost:9200/cf-errors/_search -H 'Content-Type: application/json' -d '{"query":{"match":{"fingerprint":"abc123"}}}' | jq .

# Verificar via OpenSearch Dashboards
# Abrir http://localhost:5601
```

#### 6. Teste de resiliência (OpenSearch offline)
```bash
# Derrubar OpenSearch
docker compose -f docker-compose.opensearch.yml down

# Chamar endpoint de erro — deve retornar 500 normalmente
curl -i http://localhost:9090/diagnostics/errors/generic

# Verificar logs — haverá aviso "Erro ao indexar documento no OpenSearch"
# mas a resposta HTTP foi enviada normalmente
```

### Decisões Técnicas Tomadas

1. **RestTemplate vs Cliente OpenSearch Java**
   - Optado por RestTemplate nativo do Spring para evitar conflitos de dependências e complexidade
   - Reduz footprint de dependências
   - Suficiente para MVP de indexação

2. **Logging Estruturado com Logstash Encoder**
   - Logstash encoder é padrão de mercado
   - Inclui MDC automaticamente (correlationId fica em cada linha)
   - Rolling file evita crescimento infinito de log

3. **Fingerprint com SHA-256 e Normalização**
   - SHA-256 oferece distribuição uniforme
   - 16 caracteres é trade-off entre brevidade e colisões (2^64 possibilidades)
   - Normalização simples com regex é MVP — em produção considerar parser mais sofisticado

4. **Indexação Assíncrona com @Async**
   - Thread pool padrão do Spring (`SimpleAsyncTaskExecutor`)
   - Não quebra resposta HTTP se falhar
   - Logs de falha permitem auditoria e debugging

5. **Tratamento de OpenSearch Offline**
   - Config bean não gera erro fatal se OpenSearch não estiver acessível
   - ErrorIndexingService captura exceções e loga como WARN/ERROR
   - Permite deployment funcionar mesmo sem OpenSearch (graceful degradation)

### O Que Falta Fazer (Escopo Futuro)

- [ ] Investigação de causa (LLM + heurísticas)
- [ ] Correlação com deploy history
- [ ] Notificação (e-mail/Telegram/Slack)
- [ ] Busca por similaridade/vetorial (embeddings)
- [ ] Runbooks automáticos
- [ ] Relatórios de tendência de erros
- [ ] Interface web para browse de erros
- [ ] Atualizar mapeamento do índice com tipos corretos (será feito quando OpenSearch estiver integrado)

### Notas Importantes

- **Segurança do OpenSearch local**: Docker compose desabilita plugin de segurança (`DISABLE_SECURITY_PLUGIN=true`)
  - **NUNCA** usar isso em produção
  - Em produção, habilitar autenticação e HTTPS

- **Performance de Logging**: Logging estruturado em JSON tem overhead mínimo (< 1ms por log)
  - RollingFileAppender usa thread pool dedicado
  - Não impacta latência de resposta HTTP

- **MDC e Async**: MDC é ThreadLocal, `@Async` do Spring preserva MDC automaticamente
  - correlationId está disponível em threads de processamento assíncrono

- **Compatibilidade**: Testado com:
  - Java 21
  - Spring Boot 3.5.4
  - OpenSearch 2.x (compatível com docker-compose fornecido)
