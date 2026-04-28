# SportSnap API

Ecossistema digital que une performance esportiva real a fotografia profissional. O core business esta na mecanica de **Shadow Stats / Reveal**: treinos geram progresso "latente" que so e validado e exposto publicamente quando o atleta adquire uma licenca de imagem capturada por um fotografo.

## Arquitetura

O sistema e composto por **3 microservicos** independentes que se comunicam via REST:

```
sportsnap-api/
├── sportsnap-gamification-service   (porta 8081) — Core Domain
├── sportsnap-marketplace-service    (porta 8082) — Supporting Domain
├── sportsnap-session-service        (porta 8083) — Generic Domain
└── sportsnap-web                    (porta 8080) — Frontend Next.js
```

| Servico | Responsabilidade |
|---|---|
| **Gamification** | Shadow Stats, Reveal/Sincronizacao, Ranking, Calculo de Overall |
| **Marketplace** | Fotos, Licencas de Imagem, Split Financeiro, Upload em Lote |
| **Session** | Spots, Sessoes, Check-ins, Registros de Atividade, Motor de Match |

Cada servico possui seu proprio banco PostgreSQL, garantindo isolamento de dados.

## Stack Tecnologica

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 3.4.4 |
| Frontend | Next.js 14 + Tailwind CSS + TypeScript |
| Persistencia | JPA / Hibernate + PostgreSQL 16 |
| Testes BDD | Cucumber 7.20 + JUnit 5 |
| Build | Maven (backend) + npm (frontend) |
| Containerizacao | Docker + Docker Compose |
| Arquitetura | Clean Architecture + DDD |

## Pre-requisitos

- **Java 21** (JDK)
- **Maven 3.9+** (ou use o wrapper `./mvnw`)
- **Node.js 20+** e **npm** (para o frontend)
- **Docker** e **Docker Compose**

## Como Executar

### Com Docker (recomendado)

```bash
# 1. Compilar os JARs
./mvnw clean package -DskipTests

# 2. Subir todos os servicos + bancos de dados + frontend
docker compose up --build

# 3. Subir apenas um servico especifico
docker compose up gamification-service
```

### Sem Docker (desenvolvimento local)

Voce precisa de um PostgreSQL rodando localmente. Configure as variaveis de ambiente ou edite os `application.yml`.

```bash
# Compilar todos os modulos
./mvnw clean compile

# Rodar um servico especifico
cd sportsnap-gamification-service
../mvnw spring-boot:run
```

### Portas dos Servicos

| Servico | Porta App | Porta DB (host) |
|---|---|---|
| **Web (Frontend)** | **8080** | — |
| Gamification | 8081 | 5433 |
| Marketplace | 8082 | 5434 |
| Session | 8083 | 5435 |

## Testes

```bash
# Rodar todos os testes
./mvnw test

# Rodar apenas testes BDD (Cucumber)
./mvnw test -Pcucumber

# Rodar testes de um servico especifico
./mvnw test -pl sportsnap-gamification-service
```

## Estrutura de Pacotes (Clean Architecture)

Cada servico segue a mesma estrutura:

```
com.sportsnap.<servico>/
├── domain/
│   ├── entities/          # Entidades JPA (Atleta, Foto, Session...)
│   ├── usecases/          # Interfaces de casos de uso
│   └── repositories/      # Interfaces (ports) — sem Spring
├── infrastructure/
│   ├── persistence/       # Implementacoes JPA (adapters)
│   ├── web/               # Controllers REST
│   └── messaging/         # Comunicacao entre servicos
└── application/           # DTOs, mappers, configuracoes
```

## Variaveis de Ambiente

| Variavel | Descricao | Default |
|---|---|---|
| `DB_HOST` | Host do banco PostgreSQL | `localhost` |
| `DB_PORT` | Porta do banco | `5432` |
| `DB_NAME` | Nome do banco | `sportsnap_<servico>` |
| `DB_USER` | Usuario do banco | `sportsnap` |
| `DB_PASS` | Senha do banco | `sportsnap` |

## Equipe

- **Antonio Paes** — [@AntonioPaess](https://github.com/AntonioPaess)
- **Galileu Moares** — [@GalileuCMMoares](https://github.com/GalileuCMMoares)
- **Marco Maciel** — [@oMarcoMaciel](https://github.com/oMarcoMaciel)
- **jhrvo0** — [@jhrvo0](https://github.com/jhrvo0)

**Disciplina:** Engenharia de Requisitos + Computacao Concorrente, Paralela e Distribuida  
**Instituicao:** CESAR School
