# language: pt

Funcionalidade: H11 - Analise de Desempenho do Atleta

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Percentil posiciona o atributo na base elegivel
    Dado que existe base elegivel no atributo "Resistencia" com valores "10,20,30,40,50"
    Quando consulto o percentil do Atleta 5 no atributo "Resistencia"
    Entao o percentil e "80.0"

  Cenario: Percentil com amostra insuficiente e indisponivel
    Dado que existe base elegivel no atributo "Resistencia" com valores "10,20,30"
    Quando consulto o percentil do Atleta 3 no atributo "Resistencia"
    Entao a analise e indisponivel

  Cenario: Similaridade retorna o atleta mais proximo
    Dado que existe base elegivel no atributo "Resistencia" com valores "50,52,90"
    Quando consulto os atletas similares ao Atleta 1 na modalidade "corrida"
    Entao o atleta mais similar ao Atleta 1 e o Atleta 2

  Cenario: Projecao estima crescimento a partir do historico
    Dado que o Atleta 1 tem Carta sincronizada com Overall 60 e 3 registros de evolucao crescente
    Quando consulto a projecao do Atleta 1
    Entao a projecao do Atleta 1 indica crescimento

  Cenario: Projecao com historico insuficiente e indisponivel
    Dado que o Atleta 1 tem Carta sincronizada com Overall 60 e 2 registros de evolucao crescente
    Quando consulto a projecao do Atleta 1
    Entao a analise e indisponivel

  Cenario: Analise indisponivel para atleta sem carta sincronizada
    Dado que o Atleta 1 tem Carta nao sincronizada no atributo "Resistencia" valor 50
    Quando consulto o percentil do Atleta 1 no atributo "Resistencia"
    Entao a analise e indisponivel
