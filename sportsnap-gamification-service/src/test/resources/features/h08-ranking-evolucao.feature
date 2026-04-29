# language: pt

Funcionalidade: H08 - Ranking e Evolucao da Carta

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Overall e calculado como media ponderada
    Dado que o Atleta 1 possui atributo "Resistencia" valor 80 peso 2 esporte "corrida"
    E o Atleta 1 possui atributo "Velocidade" valor 60 peso 1 esporte "corrida"
    Quando calculo o Overall do Atleta 1
    Entao o Overall e "73.33"

  Cenario: Ranking global ordena por Overall decrescente
    Dado que o Atleta 1 tem Carta sincronizada com Overall 75
    E o Atleta 2 tem Carta sincronizada com Overall 90
    Quando consulto o ranking global
    Entao o primeiro do ranking e o Atleta 2
    E o segundo do ranking e o Atleta 1

  Cenario: Apenas cartas sincronizadas aparecem no ranking
    Dado que o Atleta 1 tem Carta sincronizada com Overall 80
    E o Atleta 2 tem Carta nao sincronizada com Overall 95
    Quando consulto o ranking global
    Entao o ranking tem 1 atleta

  Cenario: Atleta consulta sua posicao no ranking
    Dado que o Atleta 1 tem Carta sincronizada com Overall 75
    E o Atleta 2 tem Carta sincronizada com Overall 90
    Quando consulto a posicao do Atleta 1 no ranking
    Entao a posicao e 2

  Cenario: Atleta nao sincronizado nao tem posicao no ranking
    Dado que o Atleta 1 tem Carta nao sincronizada com Overall 80
    Quando consulto a posicao do Atleta 1 no ranking
    Entao a posicao nao esta definida

  Cenario: Comparar cartas de dois atletas
    Dado que o Atleta 1 tem Carta sincronizada com Overall 80
    E o Atleta 2 tem Carta sincronizada com Overall 70
    Quando comparo as Cartas dos Atletas 1 e 2
    Entao a comparacao retorna 2 cartas
