# SportSnap API

Ecossistema digital que une performance esportiva real a fotografia profissional. Atletas registram treinos, fotógrafos capturam momentos esportivos, e a plataforma conecta os dois através de licenciamento de imagens e uma camada social completa.

## Arquitetura

O sistema é composto por **3 microsserviços** independentes seguindo **Clean Architecture** e **DDD**:

```
sportsnap-api/
├── sportsnap-social-service      — Core Domain (Social, Perfil, Feed, Conexões)
├── sportsnap-marketplace-service — Supporting Domain (Fotos, Licenças, Marketplace)
├── sportsnap-session-service     — Generic Domain (Spots, Sessões, Check-ins)
└── docs/                         — Documentação do projeto
```

| Módulo | Responsabilidade | Porta |
|---|---|---|
| **Social** | Perfil social, conexões, feed, posts esportivos, comentários, notificações, ranking, sincronização de carta | 8081 |
| **Marketplace** | Fotos, lotes, licenças de imagem, split financeiro, motor de sugestão, assinatura | 8082 |
| **Session** | Spots, sessões, check-ins, registros de atividade real | 8083 |

## Histórias por Integrante

| Integrante | Histórias |
|---|---|
| **Antônio Paes** | H03 — Gerenciar Lotes de Fotos · H04 — Dashboard do Fotógrafo |
| **Galileu Calaça** | H09 — Perfil Social e Rede de Conexões · H10 — Feed, Posts Esportivos e Notificações |
| **Marco Maciel** | H05 — Comprar Licença · H06 — Motor de Sugestão |
| **João Henrique** | H01 — Gerenciar Sessão · H02 — Check-in e Registro Real de Atividade |

## Padrões de Projeto GoF Implementados

| Padrão | Integrante | Serviço | Arquivos `.java` envolvidos |
|---|---|---|---|
| **Strategy** | Galileu Calaça | Social | `dominio/conexao/ConexaoServico.java` (algoritmo de sugestão por score ponderado) |
| **Observer** | Galileu Calaça | Social | `dominio/evento/EventoBarramento.java` (interface), `infraestrutura/evento/EventoBarramentoSpring.java`, `infraestrutura/evento/SocialEventoListener.java` |
| **Repository** | Galileu Calaça | Social | `dominio/perfil/PerfilRepositorio.java`, `dominio/conexao/ConexaoRepositorio.java`, `dominio/feed/ItemFeedRepositorio.java`, `dominio/notificacao/NotificacaoRepositorio.java` (interfaces no domínio, implementações em infraestrutura) |
| **Decorator** | Marco Maciel | Marketplace | `dominio/foto/FotoDecorador.java`, `dominio/foto/FotoComMarcaDagua.java`, `dominio/foto/FotoPreviewBasico.java` |

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.4.4 |
| ORM | JPA (Spring Data JPA) + Flyway |
| Banco de dados | H2 in-memory |
| Testes BDD | Cucumber 7.20 + JUnit 5 |
| Validação de domínio | Apache Commons Lang3 (`Validate`) |
| Build | Maven (wrapper incluído) |
| Frontend | Next.js 14 (App Router) + TypeScript + TailwindCSS |
| Arquitetura | Clean Architecture + DDD |

## Pré-requisitos

- **Java 17+** (JDK)
- **Maven 3.9+** (ou use o wrapper `./mvnw`)
- **Node.js 18+** + npm (para o frontend)

## Como Executar

```bash
# Terminal 1 — Social (porta 8081)
./mvnw -pl sportsnap-social-service spring-boot:run

# Terminal 2 — Marketplace (porta 8082)
./mvnw -pl sportsnap-marketplace-service spring-boot:run

# Terminal 3 — Session (porta 8083)
./mvnw -pl sportsnap-session-service spring-boot:run

# Terminal 4 — Frontend (porta 3000)
cd sportsnap-web && npm run dev
```

## Como Executar os Testes BDD

```bash
# Serviço Social (H07-H12)
./mvnw -pl sportsnap-social-service test

# Serviço Session (H01-H02)
./mvnw -pl sportsnap-session-service test

# Serviço Marketplace (H03-H06)
./mvnw -pl sportsnap-marketplace-service test
```

## Estrutura de Pacotes (Clean Architecture)

Cada módulo segue a mesma estrutura, seguindo o padrão de referência do professor:

```
com.sportsnap.<servico>/
├── dominio/                    # PURO — zero dependências externas
│   ├── <contexto>/
│   │   ├── XxxId.java          # Value Object para identidade
│   │   ├── Xxx.java            # Entidade (2 construtores + Validate + Domain Events)
│   │   ├── XxxRepositorio.java # Interface (port)
│   │   └── XxxServico.java     # Serviço de domínio
│   └── evento/EventoBarramento.java
├── aplicacao/                  # DTOs e serviços de consulta (read model)
├── apresentacao/               # REST Controllers
└── infraestrutura/
    ├── persistencia/jpa/       # @Entity + JpaRepository + @Repository impl (mesmo arquivo)
    └── evento/                 # EventoBarramentoSpring
```

## Cenários BDD (Cucumber)

| Módulo | Feature | História |
|---|---|---|
| Session | `h01-gerenciar-sessao.feature` | H01 — Gerenciar Sessão de Treino |
| Session | `h02-checkin-atividade.feature` | H02 — Check-in e Atividade |
| Marketplace | `h03-gerenciar-lote.feature` | H03 — Gerenciar Lotes de Fotos |
| Marketplace | `h04-dashboard-fotografo.feature` | H04 — Dashboard do Fotógrafo |
| Marketplace | `h05-comprar-licenca.feature` | H05 — Comprar Licença |
| Marketplace | `h06-motor-sugestao.feature` | H06 — Motor de Sugestão |
| Social | `h09-perfil-social.feature` | H09 — Perfil Social e Rede de Conexões |
| Social | `h10-conexoes.feature` | H10 — Conexões, Pedidos e Bloqueios |
| Social | `h11-feed.feature` | H11 — Feed de Atividades e Curtidas |
| Social | `h12-notificacoes.feature` | H12 — Notificações |

## Documentação

- [`docs/dominio.md`](docs/dominio.md) — Descrição do domínio, linguagem onipresente, regras de negócio
- [`docs/user-story-map.md`](docs/user-story-map.md) — Mapa das histórias com operações detalhadas
- [`docs/prototipos.md`](docs/prototipos.md) — Protótipos de baixa e alta fidelidade
- [`docs/sportsnap.cml`](docs/sportsnap.cml) — Modelo Context Mapper (DDD)

## Equipe

- **Antônio Paes** — [@AntonioPaess](https://github.com/AntonioPaess)
- **Galileu Calaça** — [@GalileuCMMoares](https://github.com/GalileuCMMoares)
- **Marco Maciel** — [@oMarcoMaciel](https://github.com/oMarcoMaciel)
- **João Henrique** — [@jhrvo0](https://github.com/jhrvo0)

**Disciplina:** Engenharia de Requisitos
**Instituição:** CESAR School
