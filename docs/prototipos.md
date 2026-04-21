# Prototipos — SportSnap

> **Nota:** Os wireframes visuais devem ser criados em Figma ou ferramenta equivalente.
> Este documento descreve as telas e fluxos principais para guiar a criacao dos prototipos.

---

## Telas do Atleta

### T01 — Tela de Login/Cadastro
- Campos: nome, email, senha
- Opcao de cadastro como Atleta ou Fotografo

### T02 — Dashboard do Atleta
- CartaOficial em destaque (imagem + Overall + atributos)
- StatusPotencial (XP acumulado, streak)
- Botao "Sincronizar Carta" (habilitado apenas se tiver licenca valida)
- Lista de sessoes proximas

### T03 — Sessoes Disponiveis
- Lista de Sessions ativas com Spot (nome, localizacao)
- Botao "Check-in" por sessao
- Indicador de distancia ate o Spot

### T04 — Registrar Atividade
- Campos: distancia, duracao, intensidade
- Vinculado ao CheckIn ativo
- Confirmacao mostrando XP ganho

### T05 — Fotos Sugeridas
- Grid de fotos com marca d'agua (preview)
- Filtro por sessao
- Preco de cada licenca
- Botao "Comprar Licenca"

### T06 — Sincronizacao (Reveal)
- Animacao de "revelacao" da carta
- Antes/Depois dos atributos
- Novo Overall calculado
- Nova posicao no Ranking

### T07 — Ranking
- Lista de atletas ordenados por Overall
- Posicao, nome, Overall, imagem da carta
- Destaque para o atleta logado

---

## Telas do Fotografo

### T08 — Dashboard do Fotografo
- Resumo de vendas (total, mes atual)
- Lista de Lotes recentes
- Saldo disponivel

### T09 — Upload de Fotos
- Selecao de Session/Spot
- Drag-and-drop de multiplas fotos
- Barra de progresso do upload
- Extracao automatica de metadados EXIF

### T10 — Gerenciar Lotes
- Lista de Lotes com contagem de fotos
- Status de vendas por lote
- Detalhes de cada foto (vendida/disponivel)

---

## Fluxos Principais

### Fluxo 1: Atleta treina e acumula XP
```
T02 (Dashboard) → T03 (Sessoes) → Check-in → T04 (Registrar Atividade) → T02 (XP atualizado)
```

### Fluxo 2: Atleta compra foto e sincroniza
```
T05 (Fotos Sugeridas) → Comprar Licenca → T06 (Sincronizacao/Reveal) → T07 (Ranking atualizado)
```

### Fluxo 3: Fotografo publica fotos
```
T08 (Dashboard) → T09 (Upload) → Selecionar Sessao → Upload Fotos → T10 (Gerenciar)
```
