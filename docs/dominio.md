# Descricao do Dominio — SportSnap

## 1. Visao Geral

O **SportSnap** e um ecossistema digital que conecta **performance esportiva real** a **fotografia profissional**. O sistema resolve um problema concreto: atletas amadores nao tem visibilidade sobre sua evolucao, e fotografos esportivos nao tem um canal direto para monetizar seu trabalho.

A mecanica central e o **Shadow Stats / Reveal**: treinos geram progresso "latente" (invisivel publicamente) que so e validado e exposto na carta publica do atleta quando ele adquire uma licenca de imagem capturada por um fotografo durante uma sessao esportiva. Isso cria um ciclo virtuoso onde treinar, ser fotografado e comprar fotos sao acoes interconectadas.

---

## 2. Subdominios (DDD — Nivel Preliminar)

### 2.1 Core Domain: Gamificacao
O diferencial competitivo do SportSnap. Contem toda a logica de progressao do atleta, cartas esportivas e ranking.

**Justificativa:** E o que torna o SportSnap unico frente a concorrentes. Sem a mecanica de Shadow Stats/Reveal, o sistema seria apenas um marketplace de fotos.

### 2.2 Supporting Domain: Marketplace
Suporta o core business fornecendo o mecanismo de compra e venda de fotos. E necessario mas nao e o diferencial.

**Justificativa:** Marketplaces de fotos ja existem. O valor esta na integracao com a gamificacao.

### 2.3 Generic Domain: Sessoes
Funcionalidade generica de geolocalizacao, sessoes esportivas e check-ins. Poderia ser substituida por um servico externo.

**Justificativa:** Check-in por localizacao e um problema resolvido. A complexidade aqui e na integracao, nao na inovacao.

---

## 3. Linguagem Onipresente (Ubiquitous Language)

Todos os termos abaixo devem ser usados **exatamente como escritos** em codigo, documentacao e comunicacao da equipe.

### Contexto de Sessao

| Termo | Significado |
|---|---|
| **Spot** | Local geografico onde o esporte acontece (praia, pista, quadra) |
| **Session** | Evento temporal vinculado a um Spot, com horario de inicio e fim |
| **CheckIn** | Declaracao de presenca de um Atleta em uma Session |
| **RegistroDeAtividade** | Dados brutos de performance: distancia, duracao, intensidade |

### Contexto de Gamificacao (Core)

| Termo | Significado |
|---|---|
| **Atleta** | Usuario principal do sistema |
| **CartaOficial** | Stats Card publico do Atleta — contem imagem e atributos numericos |
| **AtributoEsportivo** | Dimensao de performance: Resistencia, Velocidade, Tecnica, Explosao |
| **Overall** | Media ponderada dos atributos — define posicao no Ranking |
| **StatusPotencial** (ShadowStats) | XP acumulado por treinos, ainda nao revelado publicamente |
| **Sincronizacao** (Reveal) | Processo que transfere StatusPotencial para a CartaOficial |
| **Ranking** (Leaderboard) | Classificacao dos atletas baseada **apenas** em Cartas Sincronizadas |
| **StreakDeConsistencia** | Multiplicador de XP por treinos consecutivos sem interrupcao |

### Contexto de Marketplace

| Termo | Significado |
|---|---|
| **Fotografo** | Usuario que captura e vende imagens esportivas |
| **Lote** (Album) | Agrupamento de fotos vinculado a uma Session e um Spot |
| **Foto** (Preview) | Imagem com marca d'agua; contem metadados EXIF (timestamp, localizacao) |
| **LicencaDeImagem** | Item digital adquirido pelo Atleta; **dispara a Sincronizacao** |
| **TagDeValidacao** (AcaoEpica) | Marcacao especial do Fotografo que concede bonus imediato de XP |
| **SplitFinanceiro** | Divisao automatica da receita entre Fotografo (70%) e plataforma (30%) |

---

## 4. Regras de Negocio Criticas (Invariantes)

Estas regras devem ser validadas nos **Use Cases**, nunca no banco ou controllers.

### RN01 — Invariante da Sincronizacao
> Um Atleta so pode atualizar sua CartaOficial se possuir uma LicencaDeImagem vinculada a uma atividade **posterior** a sua ultima sincronizacao.

**Impacto:** Impede que o atleta "revele" progresso sem ter comprado uma foto recente. E a regra central do modelo de negocio.

### RN02 — Regra do Match
> Uma Foto so pode ser sugerida a um Atleta se o timestamp EXIF da foto estiver **dentro do intervalo** do CheckIn realizado por ele naquela Session.

**Impacto:** Garante que fotos sao vinculadas apenas a atletas que realmente estavam presentes.

### RN03 — Integridade do Split
> Toda venda de LicencaDeImagem deve gerar **atomicamente** um credito para o Fotografo e um registro de taxa para a plataforma.

**Impacto:** Sem esta atomicidade, dinheiro pode ser perdido ou duplicado.

### RN04 — Regra de Degradacao (Opcional)
> Inatividade acima de 30 dias aplica reducao progressiva nos StatusPotencial.

**Impacto:** Incentiva treinos regulares e mantem o ranking dinamico.

---

## 5. Niveis DDD

### 5.1 Nivel Preliminar
- **Problema:** Atletas amadores nao tem visibilidade sobre evolucao; fotografos nao monetizam
- **Solucao:** Plataforma que conecta performance a fotografia via gamificacao
- **Stakeholders:** Atletas, Fotografos, Plataforma (operador)

### 5.2 Nivel Estrategico
- **Core Domain:** Gamificacao (Shadow Stats, Reveal, Ranking) — maximo investimento
- **Supporting Domain:** Marketplace (fotos, licencas, pagamentos) — necessario mas nao diferencial
- **Generic Domain:** Sessoes (spots, check-ins, geolocalizacao) — poderia ser terceirizado

### 5.3 Nivel Tatico
- **Aggregates:**
  - `Atleta` (raiz) → CartaOficial → AtributoEsportivo + StatusPotencial
  - `Fotografo` (raiz) → Lote → Foto → LicencaDeImagem → SplitFinanceiro
  - `Spot` (raiz) → Session → CheckIn → RegistroDeAtividade
- **Repositories:** Um por aggregate root (AtletaRepository, FotografoRepository, SpotRepository)
- **Services:** Use cases que orquestram logica cross-aggregate (SincronizarCartaAtleta, ProcessarVendaFoto)

### 5.4 Nivel Operacional
- **Arquitetura:** Clean Architecture com 2 camadas na 1a entrega (domain, application)
- **Persistencia:** Repositorios in-memory (sem dependencia de banco de dados na 1a entrega)
- **Testes:** BDD com Cucumber, cenarios em portugues, validando regras de negocio no dominio puro
- **Build:** Maven multi-module com 3 servicos independentes

---

## 6. Fluxo Principal do Sistema

```
1. Fotografo cria um Lote vinculado a uma Session/Spot
2. Fotografo faz upload de Fotos com metadados EXIF
3. Atleta faz CheckIn em uma Session ativa
4. Atleta treina e gera RegistroDeAtividade → acumula StatusPotencial (XP)
5. Motor de Match cruza CheckIns com timestamps EXIF das Fotos
6. Atleta visualiza Fotos sugeridas (com marca d'agua)
7. Atleta compra LicencaDeImagem → SplitFinanceiro e gerado atomicamente
8. Atleta dispara Sincronizacao → StatusPotencial transferido para CartaOficial
9. Overall e recalculado → posicao no Ranking e atualizada
```
