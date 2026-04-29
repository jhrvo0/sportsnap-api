# Mapa de Historias do Usuario — SportSnap

## Personas

| Persona | Descricao |
|---|---|
| **Atleta** | Praticante de esporte amador que quer acompanhar sua evolucao e ter fotos profissionais |
| **Fotografo** | Profissional que captura imagens em eventos esportivos e quer monetizar seu trabalho |

---

## User Story Map (Backbone)

```
ATLETA:     Treinar          Registrar Atividade    Comprar Foto       Evoluir Carta       Competir
FOTOGRAFO:  Criar Lote       Fotografar             Gerenciar Lote     Acompanhar Vendas
```

Cada backbone se desdobra em **uma historia completa** (CRUD + regras de negocio) — nao em verbos isolados.

---

## 8 Historias Completas (1a Entrega)

Cada historia abaixo e uma funcionalidade nao trivial com **multiplas operacoes** (cadastrar, consultar, editar, cancelar, validar), e nao apenas um verbo isolado. Todas as operacoes compoem uma jornada coerente do usuario.

---

### H01 — Gerenciar Sessao de Treino  *(João Henrique)*

> Como **Atleta** ou **Fotografo**, quero gerenciar Sessoes esportivas em Spots para organizar treinos e saber onde aconteceram.

**Operacoes:**
1. **Cadastrar Spot** (local fisico com coordenadas validadas)
2. **Cadastrar Sessao** vinculada a um Spot, com janela temporal (inicio < fim)
3. **Listar Sessoes ativas** (agora entre inicio e fim)
4. **Consultar Sessao por id**
5. **Cancelar Sessao** (apenas antes de iniciar)
6. **Pesquisar Sessoes por Spot ou periodo**

**Regras de negocio:**
- Sessao nao pode ter fim anterior ao inicio
- Coordenadas do Spot devem ser validas (latitude -90..90, longitude -180..180)
- Sessao cancelada nao aceita check-in
- Sessao nao pode ser cancelada se ja iniciou

---

### H02 — Gerenciar Check-in e Registro de Atividade  *(João Henrique)*

> Como **Atleta**, quero fazer check-in em uma Sessao ativa, registrar minha performance, e consultar meu historico.

**Operacoes:**
1. **Realizar CheckIn** em Sessao ativa (valida janela temporal)
2. **Registrar Atividade** vinculada ao CheckIn (distancia, duracao, intensidade)
3. **Listar meus CheckIns** (historico pessoal)
4. **Consultar Atividades de um CheckIn**
5. **Cancelar CheckIn** (apenas se nao tiver Atividade registrada)
6. **Calcular XP acumulado** por CheckIn (emite evento de dominio)

**Regras de negocio:**
- CheckIn so e aceito se `agora ∈ [sessao.inicio, sessao.fim]`
- Atleta nao pode fazer check-in duplicado na mesma Sessao
- XP = `distancia × multiplicador(intensidade)` com multiplicadores `alta=3, media=2, baixa=1`
- CheckIn com Atividade registrada nao pode ser cancelado
- Registrar Atividade emite `AtividadeRegistradaEvento` (para o contexto de Gamification)

---

### H03 — Gerenciar Lote de Fotos  *(Antônio Paes)*

> Como **Fotografo**, quero criar e gerenciar Lotes de fotos vinculados a Sessoes para disponibiliza-los ao marketplace.

**Operacoes:**
1. **Cadastrar Lote** vinculado a Sessao e Spot
2. **Upload de Fotos** em lote (extrai EXIF)
3. **Listar meus Lotes**
4. **Editar descricao** do Lote
5. **Remover Foto** do Lote (apenas se nao tiver licenca)
6. **Arquivar Lote** (congela para evitar novas vendas)

**Regras de negocio:**
- Lote precisa de Sessao e Spot validos na criacao
- Upload vazio e rejeitado
- Cada Foto precisa de timestamp EXIF (usado pelo Motor de Match)
- Foto com LicencaDeImagem emitida nao pode ser removida
- Lote arquivado nao aceita upload de novas fotos

---

### H04 — Dashboard do Fotografo  *(Antônio Paes)*

> Como **Fotografo**, quero consultar metricas consolidadas dos meus Lotes e Vendas para tomar decisoes de negocio.

**Operacoes:**
1. **Consultar resumo do Fotografo** (total de lotes, fotos, licencas vendidas, receita)
2. **Listar vendas por periodo**
3. **Consultar detalhes de uma venda** (inclui SplitFinanceiro)
4. **Consultar top fotos mais vendidas**
5. **Consultar saldo disponivel** (soma dos SplitFinanceiro do fotografo)
6. **Consultar Lote com estatisticas** (fotos, vendas, percentual convertido)

**Regras de negocio:**
- Metricas consideram apenas licencas confirmadas (nao canceladas)
- Fotografo com zero vendas ve metricas zeradas (nunca nulas)
- Top fotos e ordenado por quantidade de licencas, desempate por data mais recente
- Saldo disponivel = soma de todos os `valorFotografo` dos SplitFinanceiros

---

### H05 — Comprar Licenca de Foto com Split Financeiro  *(Marco Maciel)*

> Como **Atleta**, quero adquirir licencas de fotos onde apareco, visualizar minhas compras, e cancelar se preciso.

**Operacoes:**
1. **Processar Venda** (cria LicencaDeImagem + SplitFinanceiro atomicamente)
2. **Listar minhas Licencas** adquiridas
3. **Consultar recibo** de uma licenca (inclui breakdown do split)
4. **Cancelar compra** (apenas dentro da janela de 7 dias e sem sincronizacao consumida)
5. **Reprocessar venda apos falha** (idempotencia por `fotoId + atletaId`)
6. **Consultar total gasto pelo Atleta**

**Regras de negocio:**
- **RN03:** SplitFinanceiro e criado junto com LicencaDeImagem, atomicamente (70% fotografo / 30% plataforma com `BigDecimal HALF_UP`)
- Preco padrao e `R$29.90` por licenca
- Compra concorrente da mesma foto: apenas a primeira sucede
- Compra emite `LicencaAdquiridaEvento` (destravar sincronizacao no Gamification)
- Cancelamento restaura a foto como disponivel e reverte o SplitFinanceiro

---

### H06 — Motor de Sugestao de Fotos  *(Marco Maciel)*

> Como **Atleta**, quero ver fotos sugeridas automaticamente com base nos meus check-ins e gerenciar meus favoritos.

**Operacoes:**
1. **Solicitar sugestoes** para um Atleta (cruza CheckIns ↔ EXIF das Fotos)
2. **Filtrar sugestoes por Sessao**
3. **Filtrar sugestoes por periodo**
4. **Favoritar foto sugerida**
5. **Remover dos favoritos**
6. **Listar favoritos do Atleta**

**Regras de negocio:**
- **RN02:** Foto so aparece nas sugestoes se `exif.timestamp ∈ [checkIn.horario, checkIn.horario + sessao.duracao]`
- Atleta sem CheckIn no periodo nao recebe sugestoes
- Foto ja adquirida (com LicencaDeImagem do proprio Atleta) nao reaparece nas sugestoes
- Favoritos nao afetam ranking nem recomendacoes

---

### H07 — Sincronizar Carta do Atleta (Reveal)  *(Galileu Calaça)*

> Como **Atleta**, quero sincronizar minha CartaOficial para revelar meu progresso, consultar historico de sincronizacoes, e validar pre-requisitos.

**Operacoes:**
1. **Verificar elegibilidade para sincronizacao** (RN01)
2. **Consultar shadow stats acumulados** (XP latente)
3. **Sincronizar Carta** (transfere XP → atributos, recalcula Overall)
4. **Consultar historico de sincronizacoes** do Atleta
5. **Comparar snapshot antes/depois** da ultima sincronizacao
6. **Reverter ultima sincronizacao** (compensatorio, apenas em caso de erro)

**Regras de negocio:**
- **RN01:** Sincronizacao so e permitida se existe `LicencaDeImagem` posterior a `ultimaSincronizacao`
- XP e distribuido proporcionalmente aos pesos dos AtributoEsportivo
- Overall e recalculado como media ponderada apos distribuicao
- StatusPotencial (XP) zera apos sincronizacao bem-sucedida
- Sincronizacao emite `CartaSincronizadaEvento`

---

### H08 — Ranking e Evolucao da Carta  *(Galileu Calaça)*

> Como **Atleta**, quero consultar o ranking, minha posicao, e a evolucao da minha carta ao longo do tempo.

**Operacoes:**
1. **Calcular Overall** de uma carta (media ponderada dos atributos)
2. **Consultar ranking global** (ordenado por Overall decrescente)
3. **Consultar ranking por modalidade** (filtra atributos por tipoEsporte)
4. **Consultar minha posicao** no ranking
5. **Consultar evolucao da minha carta** (Overall ao longo das sincronizacoes)
6. **Comparar cartas** de dois atletas

**Regras de negocio:**
- Apenas cartas com `sincronizada = true` entram no ranking
- `Overall = Soma(atributo.valor × atributo.peso) / Soma(atributo.peso)`
- Desempate no ranking: Atleta com sincronizacao mais recente primeiro
- Atleta nao sincronizado consulta seu potencial mas nao aparece no ranking
- Historico de evolucao guarda snapshot do Overall apos cada sincronizacao

---

## Matriz de Responsabilidade

| Integrante | Historias |
|---|---|
| **Antônio Paes** | H03 (Gerenciar Lote de Fotos) + H04 (Dashboard do Fotografo) |
| **Galileu Calaça** | H07 (Sincronizar Carta) + H08 (Ranking e Evolucao) |
| **Marco Maciel** | H05 (Comprar Licenca com Split) + H06 (Motor de Sugestao) |
| **João Henrique** | H01 (Gerenciar Sessao de Treino) + H02 (Check-in e Atividade) |

---

## Prioridade de Implementacao

**Release 1 (1a Entrega — Dominio puro + BDD):**
**Escopo:** H01 ate H08 — 8 historias completas com todas as suas operacoes.
**Objetivo:** Implementar **todas as operacoes** de cada historia no Dominio puro (sem JPA, sem Web). Cobrir cada operacao com cenarios BDD em Cucumber. Validar regras de negocio (RN01, RN02, RN03) como invariantes de dominio.

**Release 2 (2a Entrega — Infraestrutura, Web e Padroes):**
**Escopo:** Integracao das 8 historias com infraestrutura real.
**Objetivo:**
  - Camada de persistencia com JPA isolada em `infraestrutura/` (Mapeadores ModelMapper traduzindo entidades JPA ⇄ entidades de dominio).
  - Camada Web (Controllers REST) em `apresentacao-backend/`.
  - 6 Padroes de Design (Iterator no Ranking, Proxy com cache, Decorator de foto, Observer para eventos, Strategy de XP por esporte, Template Method de sincronizacao).
  - Comunicacao entre microservicos via REST + eventos.
  - Concorrencia explicita (optimistic locking em Foto).
