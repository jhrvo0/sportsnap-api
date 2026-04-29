# language: pt

Funcionalidade: H07 - Sincronizar Carta do Atleta

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Atleta elegivel sincroniza carta com licenca valida
    Dado que o Atleta 1 possui CartaOficial com atributos iniciais
    E o Atleta 1 acumulou 100 XP
    E o Atleta 1 possui Licenca adquirida apos a ultima sincronizacao
    Quando o Atleta 1 sincroniza a Carta
    Entao o XP do Atleta 1 fica zerado
    E a ultima sincronizacao do Atleta 1 e registrada
    E o Overall da Carta e maior que zero
    E um evento CartaSincronizadaEvento e publicado

  Cenario: Sincronizacao sem licenca valida e rejeitada
    Dado que o Atleta 2 possui CartaOficial com atributos iniciais
    E o Atleta 2 acumulou 100 XP
    Quando o Atleta 2 tenta sincronizar a Carta
    Entao a sincronizacao e rejeitada
    E a Carta do Atleta 2 nao esta sincronizada

  Cenario: Sincronizacao sem XP acumulado e rejeitada
    Dado que o Atleta 3 possui CartaOficial com atributos iniciais
    E o Atleta 3 possui Licenca adquirida apos a ultima sincronizacao
    Quando o Atleta 3 tenta sincronizar a Carta
    Entao a sincronizacao e rejeitada

  Cenario: Elegibilidade e positiva com XP e licenca
    Dado que o Atleta 4 possui CartaOficial com atributos iniciais
    E o Atleta 4 acumulou 50 XP
    E o Atleta 4 possui Licenca adquirida apos a ultima sincronizacao
    Quando consulto a elegibilidade do Atleta 4
    Entao a elegibilidade e positiva

  Cenario: Elegibilidade e negativa sem licenca
    Dado que o Atleta 5 possui CartaOficial com atributos iniciais
    E o Atleta 5 acumulou 50 XP
    Quando consulto a elegibilidade do Atleta 5
    Entao a elegibilidade e negativa

  Cenario: Shadow stats refletem XP acumulado
    Dado que o Atleta 6 possui CartaOficial com atributos iniciais
    E o Atleta 6 acumulou 75 XP
    Quando consulto os shadow stats do Atleta 6
    Entao o XP acumulado e "75.0"

  Cenario: Overall com 200 XP e maior que com 100 XP
    Dado que o Atleta 7 possui CartaOficial com atributos iniciais
    E o Atleta 7 acumulou 100 XP
    E o Atleta 7 possui Licenca adquirida apos a ultima sincronizacao
    Quando o Atleta 7 sincroniza a Carta
    E o overall da Carta do Atleta 7 e capturado como referencia
    E o Atleta 7 acumulou 200 XP
    E o Atleta 7 possui Licenca adquirida apos a ultima sincronizacao
    E o Atleta 7 sincroniza a Carta
    Entao o Overall do Atleta 7 e maior que a referencia
