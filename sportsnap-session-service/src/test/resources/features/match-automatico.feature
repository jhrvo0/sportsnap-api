# language: pt

Funcionalidade: Motor de Match Automatico e Sugestoes

  Cenario: Match encontra atletas com check-in no intervalo da sessao
    Dado que existe uma sessao ativa no Spot "Arena Recife"
    E o Atleta com id 1 realizou check-in durante a sessao
    Quando o Motor de Match e executado para a sessao
    Entao o Atleta com id 1 aparece na lista de matches

  Cenario: Atleta sem check-in nao aparece no match
    Dado que existe uma sessao ativa no Spot "Parque da Jaqueira"
    E nenhum atleta realizou check-in na sessao
    Quando o Motor de Match e executado para a sessao
    Entao a lista de matches esta vazia

  Cenario: Multiplos atletas com check-in sao retornados no match
    Dado que existe uma sessao ativa no Spot "Praia de Boa Viagem"
    E o Atleta com id 1 realizou check-in durante a sessao
    E o Atleta com id 2 realizou check-in durante a sessao
    Quando o Motor de Match e executado para a sessao
    Entao ambos os atletas aparecem na lista de matches
