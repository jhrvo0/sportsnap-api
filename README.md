# SportSnap API

Ecossistema digital que une performance esportiva real a fotografia profissional. O core business esta na mecanica de **Shadow Stats / Reveal**: treinos geram progresso "latente" que so e validado e exposto publicamente quando o atleta adquire uma licenca de imagem capturada por um fotografo.

## Arquitetura

O sistema e composto por **3 modulos** independentes seguindo **Clean Architecture** e **DDD**:

```
sportsnap-api/
├── sportsnap-gamification-service   — Core Domain
├── sportsnap-marketplace-service    — Supporting Domain
├── sportsnap-session-service        — Generic Domain
└── docs/                            — Documentacao do projeto
```

| Modulo | Responsabilidade |
|---|---|
| **Gamification** | Shadow Stats, Reveal/Sincronizacao, Ranking, Calculo de Overall |
| **Marketplace** | Fotos, Licencas de Imagem, Split Financeiro, Upload em Lote |
| **Session** | Spots, Sessoes, Check-ins, Registros de Atividade, Motor de Match |

## 1a Entrega — Foco no Dominio e BDD

Esta entrega foca na **modelagem do dominio puro** com validacao via **testes BDD (Cucumber)**. Nao ha dependencia de banco de dados, UI ou infraestrutura externa.

### Escopo

- **8 funcionalidades nao triviais** (2 por integrante), cobrindo todas as User Stories (US01-US08)
- Regras de negocio blindadas na camada de Dominio
- Repositorios in-memory para isolamento total dos testes
- Linguagem Onipresente aplicada em todo o codigo e documentacao

### Funcionalidades por Integrante

| Integrante | Funcionalidades |
|---|---|
| **Antonio** | US01 (Check-in Georreferenciado) + US02 (Registro e Calculo de Atividade) |
| **Galileu** | US03 (Upload e Extracao EXIF em Lote) + US04 (Dashboard de Controle e Conversao) |
| **Marco** | US05 (Comprar Licenca com Split Financeiro) + US06 (Motor de Match Automatico) |
| **jhrvo0** | US07 (Sincronizar Carta / Reveal) + US08 (Calculo de Overall Dinamico) |

## Stack Tecnologica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Testes BDD | Cucumber 7.20 + JUnit 5 |
| Build | Maven (wrapper incluido) |
| Arquitetura | Clean Architecture + DDD |

## Pre-requisitos

- **Java 21** (JDK)
- **Maven 3.9+** (ou use o wrapper `./mvnw`)

## Como Executar os Testes

```bash
# Rodar todos os testes (BDD + unitarios)
./mvnw clean test

# Rodar apenas testes BDD (Cucumber)
./mvnw test -Pcucumber

# Rodar testes de um modulo especifico
./mvnw test -pl sportsnap-gamification-service
./mvnw test -pl sportsnap-session-service
./mvnw test -pl sportsnap-marketplace-service
```

## Estrutura de Pacotes (Clean Architecture)

Cada modulo segue a mesma estrutura:

```
com.sportsnap.<servico>/
├── domain/
│   ├── entities/          # Entidades puras (POJOs) da Linguagem Onipresente
│   ├── usecases/          # Interfaces dos casos de uso
│   └── repositories/      # Interfaces (ports) dos repositorios
└── application/           # Implementacoes dos use cases e repositorios in-memory
```

## Cenarios BDD (Cucumber)

Cada funcionalidade possui um arquivo `.feature` em portugues com cenarios que validam as regras de negocio:

| Modulo | Feature | User Story |
|---|---|---|
| Session | `checkin.feature` | US01 — Check-in Georreferenciado |
| Session | `registro-atividade.feature` | US02 — Registro de Atividade |
| Marketplace | `upload-lote.feature` | US03 — Upload em Lote |
| Marketplace | `dashboard-fotografo.feature` | US04 — Dashboard do Fotografo |
| Marketplace | `venda-foto.feature` | US05 — Compra de Licenca |
| Session | `match-automatico.feature` | US06 — Motor de Match |
| Gamification | `sincronizacao.feature` | US07 — Sincronizacao/Reveal |
| Gamification | `calculo-overall.feature` | US08 — Calculo de Overall |

## Documentacao

- [`docs/dominio.md`](docs/dominio.md) — Descricao do dominio, linguagem onipresente, regras de negocio, niveis DDD
- [`docs/user-story-map.md`](docs/user-story-map.md) — User Story Map com detalhamento de US01-US08
- [`docs/prototipos.md`](docs/prototipos.md) — Prototipos de baixa e alta fidelidade
- [`docs/sportsnap.cml`](docs/sportsnap.cml) — Modelo Context Mapper (DDD)

## Equipe

- **Antonio Paes** — [@AntonioPaess](https://github.com/AntonioPaess)
- **Galileu Moares** — [@GalileuCMMoares](https://github.com/GalileuCMMoares)
- **Marco Maciel** — [@oMarcoMaciel](https://github.com/oMarcoMaciel)
- **jhrvo0** — [@jhrvo0](https://github.com/jhrvo0)

**Disciplina:** Engenharia de Requisitos  
**Instituicao:** CESAR School
