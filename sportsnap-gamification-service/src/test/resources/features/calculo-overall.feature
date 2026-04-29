# language: pt

Funcionalidade: Calculo de Overall Dinamico

  Cenario: Overall e calculado como media ponderada dos atributos
    Dado que o Atleta "Pedro" possui uma CartaOficial com atributos esportivos
    E o atributo "Resistencia" tem valor 80 e peso 2
    E o atributo "Velocidade" tem valor 60 e peso 1
    Quando o Overall e recalculado
    Entao o Overall deve ser a media ponderada dos atributos

  Cenario: Ranking e ordenado por Overall decrescente
    Dado que existem dois Atletas com CartaOficial sincronizada
    E o Atleta "Ana" possui Overall 75
    E o Atleta "Bruno" possui Overall 90
    Quando o Ranking e consultado
    Entao o Atleta "Bruno" aparece antes de "Ana" no Ranking

  Cenario: Apenas cartas sincronizadas participam do ranking
    Dado que o Atleta "Carlos" possui CartaOficial sincronizada com Overall 80
    E o Atleta "Diana" possui CartaOficial nao sincronizada com Overall 95
    Quando o Ranking e consultado
    Entao apenas o Atleta "Carlos" aparece no Ranking
