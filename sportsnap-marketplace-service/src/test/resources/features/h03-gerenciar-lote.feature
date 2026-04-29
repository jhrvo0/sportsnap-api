# language: pt

Funcionalidade: H03 - Gerenciar Lote de Fotos

  Contexto:
    Dado que o sistema de Marketplace esta limpo

  Cenario: Fotografo cadastra Lote vinculado a Sessao e Spot
    Dado que existe o Fotografo "Carlos" cadastrado
    Quando cadastro um Lote para a Sessao 10 e Spot 20 com descricao "Corrida da Orla"
    Entao o Lote e salvo com id

  Cenario: Cadastrar Lote sem Fotografo valido e rejeitado
    Quando tento cadastrar um Lote para Fotografo inexistente
    Entao o cadastro do Lote e rejeitado

  Cenario: Fotografo faz upload de fotos no Lote
    Dado que existe o Fotografo "Ricardo" cadastrado
    E existe um Lote do Fotografo "Ricardo"
    Quando o Fotografo faz upload de 3 fotos no Lote
    Entao 3 fotos sao registradas no Lote
    E cada foto possui metadados EXIF extraidos

  Cenario: Upload vazio e rejeitado
    Dado que existe o Fotografo "Ana" cadastrado
    E existe um Lote do Fotografo "Ana"
    Quando o Fotografo tenta fazer upload de 0 fotos no Lote
    Entao o upload e rejeitado

  Cenario: Listar Lotes do Fotografo
    Dado que existe o Fotografo "Julia" cadastrado
    E o Fotografo "Julia" possui 2 Lotes cadastrados
    Quando consulto os Lotes do Fotografo "Julia"
    Entao recebo 2 Lotes

  Cenario: Editar descricao do Lote
    Dado que existe o Fotografo "Pedro" cadastrado
    E existe um Lote do Fotografo "Pedro"
    Quando edito a descricao do Lote para "Novo titulo"
    Entao o Lote tem a descricao atualizada

  Cenario: Arquivar Lote impede novos uploads
    Dado que existe o Fotografo "Marcelo" cadastrado
    E existe um Lote do Fotografo "Marcelo"
    Quando arquivo o Lote
    E o Fotografo tenta fazer upload de 1 fotos no Lote
    Entao o upload e rejeitado

  Cenario: Remover Foto sem licenca
    Dado que existe o Fotografo "Lucas" cadastrado
    E existe um Lote do Fotografo "Lucas" com 1 foto
    Quando removo a primeira foto do Lote
    Entao a foto fica marcada como removida
