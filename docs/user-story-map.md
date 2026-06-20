# Mapa de Historias do Usuario - SportSnap

## Personas

| Persona | Descricao |
|---|---|
| **Atleta** | Praticante de esporte amador que quer acompanhar sua evolucao e ter fotos profissionais |
| **Fotografo** | Profissional que captura imagens em eventos esportivos e quer monetizar seu trabalho |

---

## User Story Map (Backbone)

```text
ATLETA:     Treinar          Registrar Atividade    Comprar Foto       Evoluir Carta       Competir
FOTOGRAFO:  Criar Lote       Fotografar             Gerenciar Lote     Acompanhar Vendas
SOCIAL:     Conectar         Compartilhar          Interagir          Receber Notificacoes
```

Cada backbone se desdobra em uma historia completa, com multiplas operacoes e regras de negocio.

---

## 12 Historias Completas

As historias abaixo cobrem o escopo funcional do SportSnap e atendem ao conjunto de BDDs do projeto.

### H01 - Gerenciar Sessao de Treino  *(Joao Henrique)*

> Como **Atleta** ou **Fotografo**, quero gerenciar sessoes esportivas em spots para organizar treinos e saber onde aconteceram.

**Operacoes:**
1. Cadastrar Spot com coordenadas validadas
2. Cadastrar Sessao vinculada a um Spot, com janela temporal
3. Listar Sessoes ativas
4. Consultar Sessao por id
5. Cancelar Sessao
6. Pesquisar Sessoes por Spot ou periodo

**Regras de negocio:**
- Sessao nao pode ter fim anterior ao inicio
- Coordenadas do Spot devem ser validas
- Sessao cancelada nao aceita check-in
- Sessao nao pode ser cancelada se ja iniciou

### H02 - Gerenciar Check-in e Registro de Atividade  *(Joao Henrique)*

> Como **Atleta**, quero fazer check-in em uma Sessao ativa, registrar minha performance e consultar meu historico.

**Operacoes:**
1. Realizar CheckIn em Sessao ativa
2. Registrar Atividade vinculada ao CheckIn
3. Listar meus CheckIns
4. Consultar Atividades de um CheckIn
5. Cancelar CheckIn
6. Calcular XP acumulado por CheckIn

**Regras de negocio:**
- CheckIn so e aceito se estiver dentro da janela da Sessao
- Atleta nao pode fazer check-in duplicado na mesma Sessao
- XP usa multiplicador por intensidade
- CheckIn com Atividade registrada nao pode ser cancelado
- Registrar Atividade emite evento de dominio para gamificacao

### H03 - Gerenciar Lote de Fotos  *(Antonio Paes)*

> Como **Fotografo**, quero criar e gerenciar lotes de fotos vinculados a sessoes para disponibiliza-los ao marketplace.

**Operacoes:**
1. Cadastrar Lote vinculado a Sessao e Spot
2. Upload de Fotos em lote
3. Listar meus Lotes
4. Editar descricao do Lote
5. Remover Foto do Lote
6. Arquivar Lote

**Regras de negocio:**
- Lote precisa de Sessao e Spot validos na criacao
- Upload vazio e rejeitado
- Cada Foto precisa de timestamp EXIF
- Foto com licenca emitida nao pode ser removida
- Lote arquivado nao aceita novas fotos

### H04 - Dashboard do Fotografo  *(Antonio Paes)*

> Como **Fotografo**, quero consultar metricas consolidadas dos meus lotes e vendas para tomar decisoes de negocio.

**Operacoes:**
1. Consultar resumo do Fotografo
2. Listar vendas por periodo
3. Consultar detalhes de uma venda
4. Consultar top fotos mais vendidas
5. Consultar saldo disponivel
6. Consultar Lote com estatisticas

**Regras de negocio:**
- Metricas consideram apenas licencas confirmadas
- Fotografo com zero vendas ve metricas zeradas
- Top fotos e ordenado por quantidade de licencas
- Saldo disponivel soma os valores do split do fotografo

### H05 - Comprar Licenca de Foto com Split Financeiro  *(Marco Maciel)*

> Como **Atleta**, quero adquirir licencas de fotos onde apareco, visualizar minhas compras e cancelar se preciso.

**Operacoes:**
1. Processar Venda
2. Listar minhas Licencas adquiridas
3. Consultar recibo de uma licenca
4. Cancelar compra
5. Reprocessar venda apos falha
6. Consultar total gasto pelo Atleta

**Regras de negocio:**
- SplitFinanceiro e criado junto com LicencaDeImagem de forma atomica
- Preco padrao da licenca e definido pelo marketplace
- Compra concorrente da mesma foto: apenas a primeira sucede
- Compra emite evento que alimenta a sincronizacao da carta
- Cancelamento restaura a foto como disponivel

### H06 - Motor de Sugestao de Fotos  *(Marco Maciel)*

> Como **Atleta**, quero ver fotos sugeridas automaticamente com base nos meus check-ins e gerenciar meus favoritos.

**Operacoes:**
1. Solicitar sugestoes para um Atleta
2. Filtrar sugestoes por Sessao
3. Filtrar sugestoes por periodo
4. Favoritar foto sugerida
5. Remover dos favoritos
6. Listar favoritos do Atleta

**Regras de negocio:**
- Foto so aparece nas sugestoes se o EXIF estiver dentro do intervalo do check-in
- Atleta sem CheckIn no periodo nao recebe sugestoes
- Foto ja adquirida nao reaparece nas sugestoes
- Favoritos nao afetam ranking nem recomendacoes

### H07 - Sincronizar Carta do Atleta (Reveal)  *(Galileu Calaca)*

> Como **Atleta**, quero sincronizar minha CartaOficial para revelar meu progresso, consultar historico de sincronizacoes e validar pre-requisitos.

**Operacoes:**
1. Verificar elegibilidade para sincronizacao
2. Consultar shadow stats acumulados
3. Sincronizar Carta
4. Consultar historico de sincronizacoes
5. Comparar snapshot antes/depois
6. Reverter ultima sincronizacao

**Regras de negocio:**
- Sincronizacao so e permitida se existir licenca posterior a ultima sincronizacao
- XP e distribuido proporcionalmente aos pesos dos atributos
- Overall e recalculado apos a distribuicao
- StatusPotencial zera apos sincronizacao bem-sucedida
- Sincronizacao emite evento de dominio

### H08 - Ranking e Evolucao da Carta  *(Galileu Calaca)*

> Como **Atleta**, quero consultar o ranking, minha posicao e a evolucao da minha carta ao longo do tempo.

**Operacoes:**
1. Calcular Overall de uma carta
2. Consultar ranking global
3. Consultar ranking por modalidade
4. Consultar minha posicao no ranking
5. Consultar evolucao da minha carta
6. Comparar cartas de dois atletas

**Regras de negocio:**
- Apenas cartas sincronizadas entram no ranking
- Overall usa media ponderada dos atributos
- Desempate no ranking usa sincronizacao mais recente
- Atleta nao sincronizado nao aparece no ranking
- Historico de evolucao guarda snapshot do Overall

### H09 - Perfil Social  *(Galileu Calaca)*

> Como **Atleta**, quero manter meu perfil social atualizado para me apresentar melhor na rede.

**Operacoes:**
1. Consultar perfil social
2. Editar bio e preferencias
3. Atualizar imagem de perfil
4. Listar estatisticas sociais basicas
5. Ver destaques do perfil
6. Exibir resumo da atividade recente

**Regras de negocio:**
- Perfil social deve validar dados obrigatorios
- Imagem de perfil deve ser uma midia valida
- Alteracoes sao refletidas no feed e nas interacoes

### H10 - Conexoes, Pedidos e Bloqueios  *(Galileu Calaca)*

> Como **Atleta**, quero me conectar com outros atletas, aceitar pedidos e bloquear contatos quando necessario.

**Operacoes:**
1. Enviar pedido de conexao
2. Aceitar ou recusar pedido
3. Remover conexao existente
4. Bloquear contato
5. Desbloquear contato
6. Listar minha rede de conexoes

**Regras de negocio:**
- Nao pode haver conexao reciproca duplicada
- Bloqueio impede novas interacoes
- Pedidos pendentes expiram conforme regra da aplicacao

### H11 - Feed de Atividades e Curtidas  *(Galileu Calaca)*

> Como **Atleta**, quero acompanhar o feed da rede e interagir com atividades de outros atletas.

**Operacoes:**
1. Listar feed
2. Publicar atividade no feed
3. Curtir postagem
4. Descurtir postagem
5. Comentar postagem
6. Filtrar feed por tipo de atividade

**Regras de negocio:**
- O feed respeita conexoes e regras de visibilidade
- Uma postagem nao pode ser curtida duas vezes pelo mesmo atleta
- Interacoes sao auditaveis e podem gerar notificacoes

### H12 - Notificacoes  *(Galileu Calaca)*

> Como **Atleta**, quero receber notificacoes sobre eventos importantes para nao perder atualizacoes da plataforma.

**Operacoes:**
1. Listar notificacoes
2. Marcar como lida
3. Marcar todas como lidas
4. Excluir notificacao
5. Filtrar por tipo
6. Consultar contador de nao lidas

**Regras de negocio:**
- Notificacoes sao geradas por eventos da plataforma
- Apenas o dono pode visualizar suas notificacoes
- Notificacoes lidas nao entram no contador de pendentes

---

## Matriz de Responsabilidade

| Integrante | Historias |
|---|---|
| **Antonio Paes** | H03 (Gerenciar Lote de Fotos) + H04 (Dashboard do Fotografo) |
| **Galileu Calaca** | H07 (Sincronizar Carta) + H08 (Ranking e Evolucao) + H09 (Perfil Social) + H10 (Conexoes) + H11 (Feed) + H12 (Notificacoes) |
| **Marco Maciel** | H05 (Comprar Licenca com Split) + H06 (Motor de Sugestao) |
| **Joao Henrique** | H01 (Gerenciar Sessao de Treino) + H02 (Check-in e Atividade) |

---

## Prioridade de Implementacao

**Release 1 (Dominio puro + BDD):**

- Escopo: H01 a H12
- Objetivo: implementar as operacoes de cada historia no dominio puro e cobrir os cenarios com Cucumber
- Foco: regras de negocio, invariantes e validacoes do dominio

**Release 2 (Infraestrutura, Web e Padroes):**

- Persistencia com JPA isolada em infraestrutura
- Camada Web com controllers REST
- Padroes de projeto documentados: Iterator, Proxy, Decorator, Observer, Strategy e Template Method
- Comunicacao entre microservicos via REST e eventos
- Concorrencia explicita onde necessario
