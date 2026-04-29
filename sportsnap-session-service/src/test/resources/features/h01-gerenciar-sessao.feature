# language: pt

Funcionalidade: H01 - Gerenciar Sessao de Treino

  Contexto:
    Dado que o sistema de Sessoes esta limpo

  Cenario: Cadastrar Spot com coordenadas validas
    Quando cadastro um Spot "Praia de Boa Viagem" com coordenadas "8.1" e "34.9"
    Entao o Spot e salvo no repositorio
    E o Spot tem um id atribuido

  Cenario: Cadastrar Spot com coordenadas invalidas e rejeitado
    Quando tento cadastrar um Spot "Spot Invalido" com latitude "200"
    Entao o cadastro do Spot e rejeitado

  Cenario: Cadastrar Sessao vinculada a um Spot existente
    Dado que existe um Spot "Arena Recife" cadastrado
    Quando cadastro uma Sessao neste Spot com duracao de 2 horas
    Entao a Sessao e salva com id
    E a Sessao aparece entre as sessoes ativas

  Cenario: Cadastrar Sessao com fim anterior ao inicio e rejeitado
    Dado que existe um Spot "Arena Recife" cadastrado
    Quando tento cadastrar uma Sessao com fim anterior ao inicio
    Entao o cadastro da Sessao e rejeitado

  Cenario: Listar Sessoes por Spot
    Dado que existe um Spot "Parque da Jaqueira" cadastrado
    E este Spot tem 2 Sessoes cadastradas
    Quando consulto as Sessoes deste Spot
    Entao recebo 2 Sessoes

  Cenario: Cancelar Sessao antes de iniciar
    Dado que existe um Spot "Praia do Paiva" cadastrado
    E existe uma Sessao futura neste Spot
    Quando cancelo a Sessao
    Entao a Sessao fica marcada como cancelada
    E a Sessao nao aparece entre as sessoes ativas
