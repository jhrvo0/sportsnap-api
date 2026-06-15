# language: pt

Funcionalidade: H10 - Ranking Competitivo, Confrontos e Temporadas

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Confronto e resolvido pelo Overall e transfere PR estilo Elo
    Dado que existe Temporada vigente para a modalidade "CORRIDA"
    E que o Atleta 1 tem Carta sincronizada com Overall 90 na modalidade "CORRIDA"
    E que o Atleta 2 tem Carta sincronizada com Overall 70 na modalidade "CORRIDA"
    Quando o Atleta 1 enfrenta o Atleta 2 na modalidade "CORRIDA"
    Entao o vencedor do confronto e o Atleta 1
    E o PR do Atleta 1 e maior que 1000
    E o PR do Atleta 2 e menor que 1000

  Cenario: Confronto fora de temporada vigente e rejeitado
    Dado que o Atleta 1 tem Carta sincronizada com Overall 90 na modalidade "CORRIDA"
    E que o Atleta 2 tem Carta sincronizada com Overall 70 na modalidade "CORRIDA"
    Quando o Atleta 1 enfrenta o Atleta 2 na modalidade "CORRIDA"
    Entao o confronto e rejeitado

  Cenario: Classificacao ordena por PR decrescente
    Dado que existe Temporada vigente para a modalidade "CORRIDA"
    E que o Atleta 1 tem Carta sincronizada com Overall 90 na modalidade "CORRIDA"
    E que o Atleta 2 tem Carta sincronizada com Overall 70 na modalidade "CORRIDA"
    E o Atleta 1 enfrenta o Atleta 2 na modalidade "CORRIDA"
    Quando consulto a classificacao competitiva
    Entao o primeiro colocado e o Atleta 1

  Cenario: Atleta vencedor consulta posicao e o perdedor fica em segundo
    Dado que existe Temporada vigente para a modalidade "CORRIDA"
    E que o Atleta 1 tem Carta sincronizada com Overall 90 na modalidade "CORRIDA"
    E que o Atleta 2 tem Carta sincronizada com Overall 70 na modalidade "CORRIDA"
    E o Atleta 1 enfrenta o Atleta 2 na modalidade "CORRIDA"
    Quando consulto a posicao competitiva do Atleta 2
    Entao a posicao competitiva e 2

  Cenario: Atleta sem carta sincronizada nao esta classificado
    Quando consulto a posicao competitiva do Atleta 9
    Entao o atleta nao esta classificado

  Cenario: Temporada com periodo valido e criada
    Quando crio uma Temporada para "NATACAO" de 1 a 30 dias no futuro
    Entao a Temporada e criada

  Cenario: Temporada sobreposta na mesma modalidade e rejeitada
    Dado que existe Temporada vigente para a modalidade "CORRIDA"
    Quando crio uma Temporada para "CORRIDA" de 1 a 30 dias no futuro
    Entao a criacao da Temporada e rejeitada

  Cenario: Temporada agendada pode ser cancelada antes de iniciar
    Dado que crio uma Temporada futura para "SURF"
    Quando cancelo essa Temporada
    Entao a Temporada esta "CANCELADA"

  Cenario: Temporada ja iniciada nao pode ser cancelada
    Dado que existe Temporada vigente para a modalidade "SURF"
    Quando cancelo essa Temporada
    Entao o cancelamento e rejeitado

  Cenario: Encerrar temporada gera snapshot final e soft-reset de PR
    Dado que existe Temporada vigente para a modalidade "CORRIDA"
    E que o Atleta 1 tem Carta sincronizada com Overall 90 na modalidade "CORRIDA"
    E que o Atleta 2 tem Carta sincronizada com Overall 70 na modalidade "CORRIDA"
    E o Atleta 1 enfrenta o Atleta 2 na modalidade "CORRIDA"
    Quando encerro essa Temporada
    Entao o snapshot final tem 2 entradas
    E o PR do Atleta 1 e maior que 1000
    E o PR do Atleta 1 e menor que 1016
