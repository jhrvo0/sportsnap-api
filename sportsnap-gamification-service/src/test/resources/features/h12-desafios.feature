# language: pt

Funcionalidade: H12 - Motor de Desafios e Insignias

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Concluir desafio por sincronizacoes concede insignia
    Dado que o Atleta 1 tem Carta sincronizada com Overall 70 na modalidade "corrida"
    E que existe um desafio permanente "Treino Firme" com meta de 2 sincronizacoes e insignia "ESFORCO"
    E o Atleta 1 aceita o ultimo desafio
    Quando o Atleta 1 registra 2 sincronizacoes
    Entao o ultimo progresso do Atleta 1 esta "CONCLUIDO"
    E o Atleta 1 possui 1 insignia

  Cenario: Insignia e concedida uma unica vez mesmo com eventos repetidos
    Dado que o Atleta 1 tem Carta sincronizada com Overall 70 na modalidade "corrida"
    E que existe um desafio permanente "Primeiro Passo" com meta de 1 sincronizacoes e insignia "INICIO"
    E o Atleta 1 aceita o ultimo desafio
    Quando o Atleta 1 registra 3 sincronizacoes
    Entao o Atleta 1 possui 1 insignia

  Cenario: Aceitar desafio fora da janela de validade e rejeitado
    Dado que existe um desafio expirado "Antigo" com insignia "VELHO"
    Quando o Atleta 1 aceita o ultimo desafio
    Entao a aceitacao e rejeitada

  Cenario: Aceitacao duplicada do mesmo desafio e rejeitada
    Dado que o Atleta 1 tem Carta sincronizada com Overall 70 na modalidade "corrida"
    E que existe um desafio permanente "Rotina" com meta de 5 sincronizacoes e insignia "ROTINA"
    E o Atleta 1 aceita o ultimo desafio
    Quando o Atleta 1 aceita o ultimo desafio
    Entao a aceitacao e rejeitada

  Cenario: Desafio encadeado exige o pre-requisito concluido
    Dado que o Atleta 1 tem Carta sincronizada com Overall 70 na modalidade "corrida"
    E que existe um desafio permanente "Base" com meta de 1 sincronizacoes e insignia "BASE"
    E que existe um desafio "Avancado" encadeado apos o ultimo definido com insignia "AVANCADO"
    Quando o Atleta 1 aceita o ultimo desafio
    Entao a aceitacao e rejeitada

  Cenario: Cancelar desafio descarta o progresso parcial
    Dado que o Atleta 1 tem Carta sincronizada com Overall 70 na modalidade "corrida"
    E que existe um desafio permanente "Maratona" com meta de 10 sincronizacoes e insignia "MARATONISTA"
    E o Atleta 1 aceita o ultimo desafio
    Quando o Atleta 1 cancela o ultimo progresso
    Entao o ultimo progresso do Atleta 1 esta "CANCELADO"
