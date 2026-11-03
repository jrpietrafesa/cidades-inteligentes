# Projeto - Cidades ESG Inteligentes

API REST em **Java 21 + Spring Boot 3** para monitoramento do consumo de energia de edifícios públicos
(escolas, hospitais, prédios administrativos). Cada leitura enviada pelos medidores inteligentes é
somada ao consumo do dia; quando o total ultrapassa a **meta ESG** do edifício, a API abre um alerta
automaticamente. O endpoint de indicadores entrega consumo total, emissão estimada de CO₂ e alertas —
o pilar **Ambiental (E)** do ESG aplicado à gestão urbana.

O projeto foi preparado para um ciclo DevOps completo: testes automatizados, imagem Docker,
orquestração com Docker Compose e pipeline CI/CD no **GitHub Actions** com deploy em **staging** e **produção**.

**Integrante:** Sidney — RM: 562855

**Repositório:** https://github.com/jrpietrafesa/cidades-inteligentes

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/edificios` | Cadastra edifício e sua meta diária (kWh) |
| GET | `/api/edificios` / `/api/edificios/{id}` | Lista / consulta edifícios |
| POST | `/api/edificios/{id}/medicoes` | Registra leitura; retorna `alertaGerado` |
| GET | `/api/edificios/{id}/medicoes` | Histórico de leituras |
| GET | `/api/alertas` | Alertas abertos |
| PATCH | `/api/alertas/{id}/resolver` | Resolve um alerta |
| GET | `/api/indicadores` | Indicadores ESG (kWh, kgCO₂, alertas) |
| GET | `/api/info` | Nome do ambiente e versão em execução |
| GET | `/actuator/health` | Health check (usado pelo Docker e pelo pipeline) |

---

## Como executar localmente com Docker

Pré-requisitos: Docker Desktop (ou Docker Engine) com Compose v2.

```bash
# 1. Criar o arquivo de variáveis
cp .env.example .env          # ajuste DB_PASSWORD se quiser

# 2. Build da imagem e subida de API + PostgreSQL
docker compose up -d --build

# 3. Acompanhar
docker compose ps
docker compose logs -f app

# 4. Testar
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/info
bash scripts/seed.sh http://localhost:8080      # dados de exemplo

# 5. Derrubar (use -v para apagar também o volume do banco)
docker compose down
```

### Simular staging e produção na mesma máquina

O mesmo `docker-compose.yml` sobe ambientes isolados, cada um com seu projeto, rede, volume e porta:

```bash
export DB_PASSWORD=minha_senha
bash scripts/deploy.sh staging        # http://localhost:8081
bash scripts/deploy.sh production     # http://localhost:8082
bash scripts/smoke-test.sh http://localhost:8081 staging
bash scripts/smoke-test.sh http://localhost:8082 production
```

### Executar os testes sem Docker

```bash
mvn verify     # testes unitários, integração, BDD, API e contrato (H2 em memória)
```

Veja também a seção [Testes automatizados](#testes-automatizados-bdd-api-e-contrato).

---

## Pipeline CI/CD

**Ferramenta:** GitHub Actions — arquivo `.github/workflows/ci-cd.yml`.
**Gatilhos:** push na `main`, pull request para a `main` e execução manual (`workflow_dispatch`).

```
push main ─► [1] Build e testes ─► [2] Imagem Docker (GHCR) ─► [3] Deploy staging ─► [4] Deploy produção
                 mvn verify            build + push               compose + smoke       aprovação manual
                                                                   test                   + compose + smoke test
pull request ─► [1] apenas (valida o código antes do merge)
```

| Etapa | O que faz |
|---|---|
| **1. Build e testes** | JDK 21 com cache Maven; `mvn -B verify` compila, executa os 44 testes (31 cenários BDD, 5 de contrato, 4 unitários e 4 de integração) e gera o JAR. Relatórios do Surefire e o JAR são publicados como artefatos; o resumo aparece na página do run. |
| **2. Imagem Docker** | Build multi-stage com Buildx e cache do GitHub; push para o GitHub Container Registry com as tags `<sha curto>` e `latest`. |
| **3. Deploy staging** | Environment `staging`. Faz pull da imagem exata do commit, sobe API + PostgreSQL com `deploy/.env.staging` (porta 8081) e aguarda os health checks (`--wait`). Em seguida o smoke test valida health, ambiente (`staging`) e o fluxo de negócio (medição acima da meta gera alerta). Logs e status dos containers viram artefato `evidencias-staging`. |
| **4. Deploy produção** | Só roda depois de staging verde e na `main`. Environment `production` com **revisor obrigatório**: o pipeline pausa até a aprovação. Mesmo processo com `deploy/.env.production` (porta 8082) e smoke test esperando `production`. |

**Configuração no GitHub (uma vez):**
1. *Settings → Environments*: criar `staging` e `production`; em `production`, marcar **Required reviewers**.
2. Em cada environment, criar o secret `DB_PASSWORD` (se ausente, o pipeline usa uma senha padrão de demonstração).
3. *Settings → Actions → General → Workflow permissions*: **Read and write**, para o push da imagem no GHCR.
4. Opcional: cadastrar um runner self-hosted e criar a variável `DEPLOY_RUNNER` com o label dele — assim os ambientes continuam no ar depois do job. Sem ela, o deploy acontece no runner do GitHub, que é descartado ao final (as evidências ficam nos artefatos).

---

## Containerização

```dockerfile
# ============================================================
# Cidades ESG Inteligentes - imagem multi-stage
# Estagio 1: build com Maven | Estagio 2: runtime somente JRE
# ============================================================

# ---------- Estagio 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copia apenas o pom primeiro para aproveitar o cache de dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Os testes ja rodam no pipeline (job build-test); aqui so empacotamos
RUN mvn -B -q package -DskipTests

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine

ARG APP_VERSION=dev
LABEL org.opencontainers.image.title="cidades-esg-inteligentes" \
      org.opencontainers.image.description="API de monitoramento energetico ESG" \
      org.opencontainers.image.version="${APP_VERSION}"

# Usuario sem privilegios (boa pratica de seguranca)
RUN addgroup -S esg && adduser -S esg -G esg
WORKDIR /app

COPY --from=build --chown=esg:esg /workspace/target/cidades-esg.jar app.jar

ENV APP_VERSION=${APP_VERSION} \
    TZ=America/Sao_Paulo \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"

USER esg
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
```

**Estratégias adotadas**

- **Multi-stage build:** o estágio Maven compila; a imagem final leva só a JRE Alpine e o JAR — bem menor e sem ferramentas de build.
- **Cache de dependências:** o `pom.xml` é copiado antes do código, então `dependency:go-offline` só roda de novo quando as dependências mudam.
- **Usuário sem privilégios** (`esg`) e `.dockerignore` para não levar `target/`, `.git` ou arquivos `.env` para a imagem.
- **HEALTHCHECK** no `/actuator/health`: o Compose usa para `--wait` e o pipeline só segue com o container saudável.
- **JVM ajustada para container:** `MaxRAMPercentage` respeita o limite de memória do container; configurável por `JAVA_OPTS`.

**Orquestração (`docker-compose.yml`)**

| Recurso | Uso |
|---|---|
| Serviços | `app` (API) e `db` (PostgreSQL 16) |
| Volume | `pgdata` persiste os dados do banco; cada ambiente (`-p esg-staging`, `-p esg-production`) tem o seu |
| Redes | `backend` **interna** (banco inacessível de fora); `frontend` só para publicar a porta da API |
| Variáveis | `.env` local, `deploy/.env.staging` e `deploy/.env.production`; senha vem de secret/shell, nunca do repositório |
| Dependência | `app` só inicia quando o `db` passa no `pg_isready` |

---

## Prints do funcionamento

Run do pipeline: https://github.com/jrpietrafesa/cidades-inteligentes/actions/runs/37866585748
Imagem publicada: https://github.com/jrpietrafesa?tab=packages

### Pipeline completo e testes
Build e testes → imagem Docker → deploy staging → deploy produção, todos com sucesso. O resumo mostra os 8 testes (4 de integração e 4 unitários) sem falhas.

![Pipeline completo](docs/prints/01-pipeline.png)

### Imagem publicada no GitHub Container Registry
![Imagem no GHCR](docs/prints/03-imagem-ghcr.png)

### Deploy em staging (porta 8081)
Smoke test com health `UP` (PostgreSQL), `"ambiente":"staging"`, medição acima da meta gerando alerta e `SMOKE TEST OK (staging)`.

![Deploy staging](docs/prints/04-deploy-staging.png)

### Aprovação manual do deploy de produção
![Aprovação de produção](docs/prints/05-aprovacao-producao.png)

### Deploy em produção (porta 8082)
Mesma versão `97500e9` validada em staging, agora com `"ambiente":"production"` e `SMOKE TEST OK (production)`.

![Deploy produção](docs/prints/06-deploy-producao.png)

### Artefatos gerados
JAR, relatórios de testes, registro do build Docker e as evidências de cada ambiente (logs, `docker compose ps` e `/api/info`).

![Artefatos](docs/prints/07-artefatos.png)

---

## Testes automatizados (BDD, API e contrato)

Os testes validam o comportamento da API com **BDD em Gherkin** (arquivos `.feature` em português), executado pelo **Cucumber**. As chamadas HTTP e as validações são feitas com **RestAssured**, e os contratos das respostas com **JSON Schema**.

| Tipo | Quantidade | Onde está |
|---|---|---|
| Cenários BDD (positivos e negativos) | 31 | `src/test/resources/features/` |
| Contratos JSON Schema | 10 | `src/test/resources/schemas/` |
| Testes que provam que os contratos reprovam respostas erradas | 5 | `src/test/java/.../bdd/ContratoJsonSchemaTest.java` |
| Passos Java (step definitions) | 3 classes | `src/test/java/br/com/fiap/esg/bdd/` |

### Cobertura das APIs

Todos os cenários validam o status code. A coluna de contrato indica o JSON Schema aplicado.

| Endpoint | Status testados | Contrato |
|---|---|---|
| `POST /api/edificios` | 201, 400 | `edificio.json`, `erro.json` |
| `GET /api/edificios` | 200 | `edificio-lista.json` |
| `GET /api/edificios/{id}` | 200, 400, 404 | `edificio.json`, `erro.json` |
| `POST /api/edificios/{id}/medicoes` | 201, 400, 404 | `medicao.json`, `erro.json` |
| `GET /api/edificios/{id}/medicoes` | 200, 404 | `medicao-lista.json`, `erro.json` |
| `GET /api/alertas` | 200 | `alerta-lista.json` |
| `PATCH /api/alertas/{id}/resolver` | 200, 404 | `alerta.json`, `erro.json` |
| `GET /api/indicadores` | 200 | `indicadores.json` |
| `GET /api/info` | 200 | `info.json` |
| `GET /actuator/health` | 200 | `health.json` |

### Como executar

**Opção 1: local, sem Docker.** Requer JDK 21 e Maven 3.9. A aplicação real sobe em porta aleatória, com H2 em memória.

```bash
bash scripts/run-tests.sh
# Relatório: target/cucumber-reports/cucumber.html
```

**Opção 2: com Docker.** Requer apenas Docker. Sobe a API e o PostgreSQL em containers e executa os testes contra eles.

```bash
bash scripts/run-tests.sh docker
# Relatório: relatorios-testes/cucumber.html
```

Para rodar parte dos cenários, use as tags dos arquivos `.feature` (`@positivo`, `@negativo`, `@ambiental`, `@governanca`, `@edificios`, `@medicoes`, `@alertas`, `@indicadores`, `@sistema`):

```bash
mvn test -Dtest=BddApiTest -Dcucumber.filter.tags="@negativo"
```

Para testar uma API que já esteja no ar, informe o endereço em `BASE_URL`. O cenário de identificação espera `APP_ENV=test` nessa API.

```bash
BASE_URL=http://localhost:8080 mvn test -Dtest=BddApiTest
```

**Opção 3: no GitHub Actions.** O workflow `.github/workflows/testes-automatizados.yml` roda a cada push nos dois ambientes e publica os relatórios como artefatos.

### Relação com ESG

- **Ambiental (`@ambiental`):** meta diária de consumo, acúmulo de leituras, geração de alertas e emissão estimada de CO₂.
- **Governança (`@governanca`):** rastreabilidade dos alertas (consumo, meta, data e status), histórico de medições e rejeição de dados inválidos.

---

## Tecnologias utilizadas

- **Linguagem/Framework:** Java 21, Spring Boot 3.3 (Web, Data JPA, Validation, Actuator)
- **Banco de dados:** PostgreSQL 16 (execução) e H2 (testes)
- **Testes:** Cucumber (Gherkin), RestAssured, JSON Schema Validator, JUnit 5, Mockito, AssertJ, Spring MockMvc
- **Build:** Maven 3.9
- **Containers:** Docker (multi-stage, Alpine JRE), Docker Compose v2
- **CI/CD:** GitHub Actions, GitHub Environments (aprovação manual), GitHub Container Registry
- **Scripts:** Bash (deploy, smoke test, seed)

---

## Estrutura do projeto

```
cidades-esg-inteligentes/
├── Dockerfile
├── docker-compose.yml
├── Dockerfile.tests         # imagem que executa os testes
├── docker-compose.test.yml  # API + PostgreSQL + testes
├── .dockerignore
├── .env.example
├── pom.xml
├── README.md
├── .github/workflows/      # ci-cd.yml e testes-automatizados.yml
├── deploy/                 # variáveis de staging e produção
├── scripts/                # deploy.sh, smoke-test.sh, seed.sh, run-tests.sh
├── docs/                   # documentação PDF e prints
└── src/
    ├── main/java/br/com/fiap/esg/   # model, repository, service, controller, dto, exception
    ├── test/java/br/com/fiap/esg/   # testes unitários, de integração e passos BDD
    └── test/resources/              # features Gherkin e contratos JSON Schema
```

---

## Checklist de entrega

| Item | OK |
|---|---|
| Projeto compactado em .ZIP com estrutura organizada | ☑ |
| Dockerfile funcional | ☑ |
| docker-compose.yml ou arquivos Kubernetes | ☑ |
| Pipeline com etapas de build, teste e deploy | ☑ |
| README.md com instruções e prints | ☑ |
| Documentação técnica com evidências (PDF ou PPT) | ☑ |
| Deploy realizado nos ambientes staging e produção | ☑ |
