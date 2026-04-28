# Arquitetura Distribuida — SportSnap

## Diagrama

```
┌─────────────────────────────────────────────────────────────────────┐
│                          DOCKER COMPOSE                            │
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │   GAMIFICATION   │  │   MARKETPLACE    │  │     SESSION      │  │
│  │   SERVICE        │  │   SERVICE        │  │     SERVICE      │  │
│  │   :8081          │  │   :8082          │  │     :8083        │  │
│  │                  │  │                  │  │                  │  │
│  │  ┌────────────┐  │  │  ┌────────────┐  │  │  ┌────────────┐  │
│  │  │ Controller │  │  │  │ Controller │  │  │  │ Controller │  │
│  │  │  REST API  │  │  │  │  REST API  │  │  │  │  REST API  │  │
│  │  └─────┬──────┘  │  │  └─────┬──────┘  │  │  └─────┬──────┘  │
│  │        │         │  │        │         │  │        │         │  │
│  │  ┌─────▼──────┐  │  │  ┌─────▼──────┐  │  │  ┌─────▼──────┐  │
│  │  │ Use Cases  │  │  │  │ Use Cases  │  │  │  │ Use Cases  │  │
│  │  │ (domain)   │  │  │  │ (domain)   │  │  │  │ (domain)   │  │
│  │  └─────┬──────┘  │  │  └─────┬──────┘  │  │  └─────┬──────┘  │
│  │        │         │  │        │         │  │        │         │  │
│  │  ┌─────▼──────┐  │  │  ┌─────▼──────┐  │  │  ┌─────▼──────┐  │
│  │  │ JPA/Spring │  │  │  │ JPA/Spring │  │  │  │ JPA/Spring │  │
│  │  │   Data     │  │  │  │   Data     │  │  │  │   Data     │  │
│  │  └─────┬──────┘  │  │  └─────┬──────┘  │  │  └─────┬──────┘  │
│  │        │         │  │        │         │  │        │         │  │
│  └────────┼─────────┘  └────────┼─────────┘  └────────┼─────────┘  │
│           │                     │                     │             │
│  ┌────────▼─────────┐  ┌───────▼──────────┐  ┌───────▼──────────┐ │
│  │  PostgreSQL 16   │  │  PostgreSQL 16   │  │  PostgreSQL 16   │ │
│  │  :5433 (host)    │  │  :5434 (host)    │  │  :5435 (host)    │ │
│  │  gamification_db │  │  marketplace_db  │  │  session_db      │ │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘

                    COMUNICACAO REST ENTRE SERVICOS

  Gamification ──GET /api/marketplace/atletas/{id}/licencas──▶ Marketplace
  Gamification ──GET /api/sessoes/sessions/{id}/match────────▶ Session
  Marketplace  ──GET /api/sessoes/sessions/{id}/checkins─────▶ Session
```

## Servicos

| Servico | Porta | Banco | Dominio | Responsabilidade |
|---|---|---|---|---|
| **Gamification** | 8081 | sportsnap_gamification | Core | Shadow Stats, Reveal, Ranking, Overall |
| **Marketplace** | 8082 | sportsnap_marketplace | Supporting | Fotos, Licencas, Split Financeiro |
| **Session** | 8083 | sportsnap_session | Generic | Spots, Sessions, Check-ins, Match |
| **Web (Next.js)** | 8080 | — (sem banco) | Frontend | Interface web, consome APIs REST dos 3 servicos |

## Comunicacao entre Servicos

| De | Para | Endpoint | Proposito |
|---|---|---|---|
| Gamification | Marketplace | `GET /api/marketplace/atletas/{id}/licencas` | Verificar se atleta possui licenca valida para sincronizacao |
| Gamification | Session | `GET /api/sessoes/sessions/{id}/match` | Consultar atletas com match em uma sessao |
| Marketplace | Session | `GET /api/sessoes/sessions/{id}/checkins` | Consultar check-ins para cruzamento com fotos |

## Isolamento de Dados

Cada servico possui seu proprio banco PostgreSQL. Referencias entre servicos sao feitas via IDs simples (`Long atletaId`, `Long sessionId`), nao via foreign keys JPA. Isso garante:

- Independencia de deploy
- Escalabilidade individual
- Tolerancia a falhas parciais
