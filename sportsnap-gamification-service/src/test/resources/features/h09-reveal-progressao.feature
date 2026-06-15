# language: pt

Funcionalidade: H09 - Reveal Estrategico com Orcamento, Tiers e Evolucao

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Reveal libera orcamento proporcional ao XP e ao tier
    Dado que o Atleta 1 possui CartaOficial Bronze com atributo "Resistencia" valor 50
    E o Atleta 1 acumulou 100 XP
    E o Atleta 1 possui Licenca adquirida apos a ultima sincronizacao
    Quando o Atleta 1 inicia o Reveal
    Entao o orcamento liberado e 100

  Cenario: Confirmar Reveal aloca pontos, eleva o Overall e gera registros
    Dado que o Atleta 1 possui CartaOficial Bronze com atributo "Resistencia" valor 50
    E o Atleta 1 acumulou 100 XP
    E o Atleta 1 possui Licenca adquirida apos a ultima sincronizacao
    Quando o Atleta 1 confirma o Reveal alocando 10 pontos em "Resistencia"
    Entao o Overall do Atleta 1 e "60.0"
    E o XP do Atleta 1 fica zerado
    E o saldo de pontos do Atleta 1 e 40
    E o historico de evolucao do Atleta 1 tem 1 registro
    E um evento CartaSincronizadaEvento e publicado

  Cenario: Simulacao mostra o resultado sem alterar o estado
    Dado que o Atleta 1 possui CartaOficial Bronze com atributo "Resistencia" valor 50
    E o Atleta 1 acumulou 100 XP
    E o Atleta 1 possui Licenca adquirida apos a ultima sincronizacao
    Quando o Atleta 1 simula o Reveal alocando 10 pontos em "Resistencia"
    Entao o Overall simulado e "60.0"
    E o Overall persistido do Atleta 1 continua "50.0"
    E o XP do Atleta 1 permanece em "100.0"

  Cenario: Alocacao acima do orcamento e rejeitada
    Dado que o Atleta 1 possui CartaOficial Bronze com atributo "Resistencia" valor 50
    E o Atleta 1 acumulou 10 XP
    E o Atleta 1 possui Licenca adquirida apos a ultima sincronizacao
    Quando o Atleta 1 confirma o Reveal alocando 5 pontos em "Resistencia"
    Entao o Reveal e rejeitado

  Cenario: Alocacao acima do teto do tier Bronze e rejeitada
    Dado que o Atleta 1 possui CartaOficial Bronze com atributo "Resistencia" valor 50
    E o Atleta 1 acumulou 1000 XP
    E o Atleta 1 possui Licenca adquirida apos a ultima sincronizacao
    Quando o Atleta 1 confirma o Reveal alocando 30 pontos em "Resistencia"
    Entao o Reveal e rejeitado

  Cenario: Reveal sem licenca valida e rejeitado
    Dado que o Atleta 1 possui CartaOficial Bronze com atributo "Resistencia" valor 50
    E o Atleta 1 acumulou 100 XP
    Quando o Atleta 1 confirma o Reveal alocando 5 pontos em "Resistencia"
    Entao o Reveal e rejeitado

  Cenario: Carta arquivada nao aceita Reveal
    Dado que o Atleta 1 possui CartaOficial Bronze com atributo "Resistencia" valor 50
    E o Atleta 1 acumulou 100 XP
    E o Atleta 1 possui Licenca adquirida apos a ultima sincronizacao
    E a Carta do Atleta 1 esta arquivada
    Quando o Atleta 1 confirma o Reveal alocando 5 pontos em "Resistencia"
    Entao o Reveal e rejeitado
