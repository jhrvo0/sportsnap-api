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
| **Marketplace** | Fotos, Lotes, Licencas de Imagem, Split Financeiro, Motor de Sugestao |
| **Session** | Spots, Sessoes, Check-ins, Registros de Atividade |

## 1a Entrega — Foco no Dominio e BDD

Esta entrega foca na **modelagem do dominio puro** com validacao via **testes BDD (Cucumber)**. Nao ha dependencia de banco de dados, UI ou infraestrutura externa.

### Escopo

- **8 historias completas** (2 por integrante), cada uma com multiplas operacoes (CRUD + regras de negocio) — nao apenas verbos isolados
- Regras de negocio blindadas na camada de Dominio (entidades puras sem JPA)
- Value Objects tipados para todos os IDs (`AtletaId`, `SpotId`, `FotoId`, etc.)
- Domain Events publicados via `EventoBarramento`
- Repositorios in-memory em `infraestrutura/memoria/` para isolamento total dos testes
- Linguagem Onipresente aplicada em todo o codigo e documentacao

### Historias por Integrante

| Integrante | Historia 1 | Historia 2 |
|---|---|---|
| **Antônio Paes** | H03 — Gerenciar Lotes de Fotos | H04 — Dashboard e Metricas do Fotografo |
| **Galileu Calaça** | H07 — Sincronizar Carta do Atleta | H08 — Ranking e Evolucao |
| **Marco Maciel** | H05 — Comprar Licenca de Foto | H06 — Motor de Sugestao de Fotos |
| **João Henrique** | H01 — Gerenciar Sessao de Treino | H02 — Gerenciar Check-in e Atividade |

## Stack Tecnologica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Testes BDD | Cucumber 7.20 + JUnit 5 |
| Validacao de dominio | Apache Commons Lang3 (`Validate`) |
| Build | Maven (wrapper incluido) |
| Arquitetura | Clean Architecture + DDD |

## Pre-requisitos

- **Java 21** (JDK)
- **Maven 3.9+** (ou use o wrapper `./mvnw`)

## Como Executar os Testes

```bash
# Rodar todos os testes (BDD + contexto Spring)
./mvnw clean test

# Rodar apenas testes BDD (Cucumber)
./mvnw test -Pcucumber

# Rodar testes de um modulo especifico
./mvnw test -pl sportsnap-gamification-service
./mvnw test -pl sportsnap-session-service
./mvnw test -pl sportsnap-marketplace-service
```

## Estrutura de Pacotes (Clean Architecture)

Cada modulo segue a mesma estrutura, seguindo o padrao de referencia do professor:

```
com.sportsnap.<servico>/
├── dominio/                    # PURO — zero dependencias externas (JPA, Spring, HTTP)
│   ├── <contexto>/             # sub-pacote por bounded sub-context
│   │   ├── XxxId.java          # Value Object para identidade
│   │   ├── Xxx.java            # Entidade (2 construtores + Validate + Domain Events)
│   │   ├── XxxRepositorio.java # interface (port)
│   │   └── XxxServico.java     # servico de dominio
│   └── evento/EventoBarramento.java
└── infraestrutura/
    ├── memoria/XxxRepositorioMemoria.java   # adapter in-memory (1a entrega)
    └── evento/EventoBarramentoSpring.java   # publica via ApplicationEventPublisher
```

## Cenarios BDD (Cucumber)

Cada historia possui um arquivo `.feature` em portugues com multiplos cenarios (5 a 9 por historia) cobrindo golden path, edge cases e regras de negocio:

| Modulo | Feature | Historia |
|---|---|---|
| Session | `h01-gerenciar-sessao.feature` | H01 — Gerenciar Sessao de Treino |
| Session | `h02-checkin-atividade.feature` | H02 — Gerenciar Check-in e Atividade |
| Marketplace | `h03-gerenciar-lote.feature` | H03 — Gerenciar Lotes de Fotos |
| Marketplace | `h04-dashboard-fotografo.feature` | H04 — Dashboard do Fotografo |
| Marketplace | `h05-comprar-licenca.feature` | H05 — Comprar Licenca de Foto |
| Marketplace | `h06-motor-sugestao.feature` | H06 — Motor de Sugestao |
| Gamification | `h07-sincronizar-carta.feature` | H07 — Sincronizar Carta do Atleta |
| Gamification | `h08-ranking-evolucao.feature` | H08 — Ranking e Evolucao |

## Documentacao

- [`docs/dominio.md`](docs/dominio.md) — Descricao do dominio, linguagem onipresente, regras de negocio, niveis DDD
- [`docs/user-story-map.md`](docs/user-story-map.md) — Mapa das 8 historias completas (H01-H08) com operacoes detalhadas
- [`docs/prototipos.md`](docs/prototipos.md) — Prototipos de baixa e alta fidelidade
- [`docs/sportsnap.cml`](docs/sportsnap.cml) — Modelo Context Mapper (DDD)

## Equipe

- **Antônio Paes** — [@AntonioPaess](https://github.com/AntonioPaess)
- **Galileu Calaça** — [@GalileuCMMoares](https://github.com/GalileuCMMoares)
- **Marco Maciel** — [@oMarcoMaciel](https://github.com/oMarcoMaciel)
- **João Henrique** — [@jhrvo0](https://github.com/jhrvo0)

**Disciplina:** Engenharia de Requisitos  
**Instituicao:** CESAR School
