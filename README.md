# SportSnap API

SportSnap e uma plataforma web que conecta performance esportiva real, fotografia profissional e rede social.

## Arquitetura

O repositorio esta organizado em 3 microservicos seguindo Clean Architecture e DDD:

- `sportsnap-session-service`: spots, sessoes, check-ins e registro real de atividades
- `sportsnap-marketplace-service`: lotes, fotos, licencas, sugestao de fotos e split financeiro
- `sportsnap-social-service`: perfil social, conexoes, feed, notificacoes, sincronizacao de carta e ranking

## Stack

- Java 21
- Spring Boot 3.4.4
- Spring Data JPA + Flyway
- H2 em memoria para testes
- Cucumber 7.20 + JUnit 5
- Next.js 14 com TypeScript e TailwindCSS

## Historias por integrante

- `Joao Henrique`: H01 - Gerenciar Sessao de Treino, H02 - Check-in e Registro de Atividade, H03 - Registro Real de Atividades
- `Antonio Paes`: H04 - Gerenciar Lote de Fotos, H05 - Dashboard do Fotografo
- `Marco Maciel`: H06 - Comprar Licenca de Foto com Split Financeiro, H07 - Plano de assinaturas
- `Galileu Calaca`: H08 - Sincronizar Carta do Atleta, H09 - Ranking e Evolucao da Carta, H10 - Perfil Social e Rede de Conexoes, H11 - Conexoes, Pedidos e Bloqueios, H12 - Feed de Atividades e Curtidas, H13 - Notificacoes

## Historias BDD

O projeto possui 13 features BDD principais, porque o numero `H03` aparece em dois contextos diferentes.

| Modulo | Feature | Historias |
| --- | --- | --- |
| Session | `h01-gerenciar-sessao.feature` | H01 - Gerenciar Sessao de Treino |
| Session | `h02-checkin-atividade.feature` | H02 - Check-in e Registro de Atividade |
| Session | `h03-registro-real-atividade.feature` | H03 - Registro Real de Atividades |
| Marketplace | `h03-gerenciar-lote.feature` | H03 - Gerenciar Lote de Fotos |
| Marketplace | `h04-dashboard-fotografo.feature` | H04 - Dashboard do Fotografo |
| Marketplace | `h05-comprar-licenca.feature` | H05 - Comprar Licenca de Foto com Split Financeiro |
| Marketplace | `h06-motor-sugestao.feature` | H06 - Motor de Sugestao de Fotos |
| Social | `h07-sincronizar-carta.feature` | H07 - Sincronizar Carta do Atleta |
| Social | `h08-ranking-evolucao.feature` | H08 - Ranking e Evolucao da Carta |
| Social | `h09-perfil-social.feature` | H09 - Perfil Social e Rede de Conexoes |
| Social | `h10-conexoes.feature` | H10 - Conexoes, Pedidos e Bloqueios |
| Social | `h11-feed.feature` | H11 - Feed de Atividades e Curtidas |
| Social | `h12-notificacoes.feature` | H12 - Notificacoes |

## Ordem de apresentacao sugerida

Para seguir o enunciado, a apresentacao pode ser feita nesta ordem:

1. Entidades independentes e base do dominio: `Spot`, `Fotografo` e `Atleta/Perfil`
2. Entidades que dependem das anteriores: `Sessao`, `Lote`, `Conexao` e `PedidoConexao`
3. Fluxos dependentes das entidades principais: `CheckIn`, `RegistroAtividade`, `Foto`, `LicencaDeImagem`, `Feed` e `Notificacao`
4. Funcionalidades de consolidacao: `Sincronizacao`, `Ranking`, `Evolucao Real` e `Dashboard`

## Documentacao

- [docs/dominio.md](docs/dominio.md): dominio, linguagem onipresente, DDD e regras de negocio
- [docs/user-story-map.md](docs/user-story-map.md): mapa das historias e operacoes
- [docs/prototipos.md](docs/prototipos.md): prototipos e fluxos de interface
- [docs/sportsnap.cml](docs/sportsnap.cml): modelo Context Mapper
- [docs/atividade_atualizada.docx](docs/atividade_atualizada.docx): documento da entrega atualizado

## Como executar

```bash
# Session
./mvnw -pl sportsnap-session-service test

# Marketplace
./mvnw -pl sportsnap-marketplace-service test

# Social
./mvnw -pl sportsnap-social-service test

# Frontend
cd sportsnap-web
npm run dev
```

## Padroes de projeto implementados

| Padrao | Integrante principal | Arquivos Java envolvidos |
| --- | --- | --- |
| `Observer` | `Joao Henrique` | `sportsnap-session-service/src/main/java/com/sportsnap/session/dominio/evento/EventoBarramento.java`, `sportsnap-session-service/src/main/java/com/sportsnap/session/infraestrutura/evento/EventoBarramentoSpring.java`, `sportsnap-session-service/src/main/java/com/sportsnap/session/dominio/checkin/CheckInServico.java`, `sportsnap-session-service/src/main/java/com/sportsnap/session/dominio/atividade/AtividadeServico.java` |
| `Decorator` | `Antonio Paes` | `sportsnap-marketplace-service/src/main/java/com/sportsnap/marketplace/dominio/foto/FotoDecorador.java`, `sportsnap-marketplace-service/src/main/java/com/sportsnap/marketplace/dominio/foto/FotoComMarcaDagua.java`, `sportsnap-marketplace-service/src/main/java/com/sportsnap/marketplace/dominio/foto/FotoPreviewBasico.java` |
| `Strategy` | `Marco Maciel` | `sportsnap-social-service/src/main/java/com/sportsnap/gamification/dominio/xp/CalculoXpEstrategia.java`, `sportsnap-social-service/src/main/java/com/sportsnap/gamification/dominio/xp/EstrategiaXpCorrida.java`, `sportsnap-social-service/src/main/java/com/sportsnap/gamification/dominio/xp/EstrategiaXpMusculacao.java` |
| `Iterator` | `Galileu Calaca` | `sportsnap-social-service/src/main/java/com/sportsnap/gamification/dominio/ranking/RankingIterador.java`, `sportsnap-social-service/src/main/java/com/sportsnap/gamification/dominio/ranking/RankingServico.java` |
| `Proxy` | `Galileu Calaca` | `sportsnap-social-service/src/main/java/com/sportsnap/gamification/dominio/ranking/RankingProxi.java` |
| `Template Method` | `Galileu Calaca` | `sportsnap-social-service/src/main/java/com/sportsnap/gamification/dominio/sincronizacao/TemplateSincronizacao.java`, `sportsnap-social-service/src/main/java/com/sportsnap/gamification/dominio/sincronizacao/SincronizacaoPadrao.java` |

Os padroes aparecem de forma distribuida entre os modulos e sustentam as regras de negocio, a extensibilidade e o desacoplamento entre dominio, infraestrutura e apresentacao.
