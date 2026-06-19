# language: pt

Funcionalidade: H03 - Registro Real de Atividades

  Contexto:
    Dado que o sistema de Sessoes esta limpo

  Cenario: Registrar uma atividade de corrida com sucesso
    Quando o Atleta 1 registra manualmente um treino de "CORRIDA" em "2026-06-14T10:00:00" com distancia "5.0" e duracao 1800
    Entao o treino e registrado com sucesso
    E o ritmo medio calculado e "6.0" min/km
    E as calorias estimadas sao "375.0" kcal
    E o XP do treino e "0.0"

  Cenario: Impedir registro com distancia negativa
    Quando o Atleta 1 tenta registrar manualmente um treino de "CORRIDA" com distancia "-5.0" e duracao 1800
    Entao o registro falha com mensagem "A distancia nao pode ser negativa"

  Cenario: Impedir registro com duracao invalida
    Quando o Atleta 1 tenta registrar manualmente um treino de "CORRIDA" com distancia "5.0" e duracao 0
    Entao o registro falha com mensagem "A duracao deve ser positiva"

  Cenario: Listar atividades de um atleta
    Dado que o Atleta 1 registrou manualmente um treino de "CORRIDA" com distancia "5.0" e duracao 1800
    E o Atleta 1 registrou manualmente um treino de "BICICLETA" com distancia "20.0" e duracao 3600
    Quando solicito a lista de atividades do Atleta 1
    Entao recebo 2 atividades no historico

  Cenario: Filtrar atividades por esporte
    Dado que o Atleta 1 registrou manualmente um treino de "CORRIDA" com distancia "5.0" e duracao 1800
    E o Atleta 1 registrou manualmente um treino de "BICICLETA" com distancia "20.0" e duracao 3600
    Quando solicito a lista de atividades do Atleta 1 filtrada pelo esporte "CORRIDA"
    Entao recebo 1 atividade no historico
    E o esporte da atividade e "CORRIDA"

  Cenario: Filtrar atividades por periodo
    Dado que o Atleta 1 registrou manualmente um treino de "CORRIDA" em "2026-06-10T10:00:00" com distancia "5.0" e duracao 1800
    E o Atleta 1 registrou manualmente um treino de "CORRIDA" em "2026-06-14T10:00:00" com distancia "6.0" e duracao 2000
    Quando solicito a lista de atividades do Atleta 1 de "CORRIDA" no periodo de "2026-06-12T00:00:00" a "2026-06-15T23:59:59"
    Entao recebo 1 atividade no historico
    E a distancia da atividade e "6.0"

  Cenario: Calcular analise de evolucao para corrida
    Dado que o Atleta 1 registrou manualmente um treino de "CORRIDA" em "2026-06-12T10:00:00" com distancia "5.0" e duracao 1800
    E o Atleta 1 registrou manualmente um treino de "CORRIDA" em "2026-06-14T10:00:00" com distancia "10.0" e duracao 3300
    Quando consulto a analise de evolucao de "CORRIDA" para o Atleta 1 no periodo de "30d"
    Entao a analise indica total de 2 treinos
    E a distancia total e "15.0" km
    E o tempo total e 5100 segundos
    E o ritmo medio geral e "5.67" min/km
    E o melhor ritmo e "5.5" min/km
    E a maior distancia e "10.0" km
    E a frequencia semanal e "0.47" treinos/semana

  Cenario: Garantir que analise real nao depende de XP
    Dado que o Atleta 1 registrou manualmente um treino de "CORRIDA" com distancia "5.0" e duracao 1800
    Quando consulto a analise de evolucao de "CORRIDA" para o Atleta 1 no periodo de "30d"
    Entao a analise real e gerada com sucesso sem conter calculo de XP

  Cenario: Garantir que registros reais nao alteram ranking diretamente
    Quando o Atleta 1 registra manualmente um treino de "CORRIDA" com distancia "5.0" e duracao 1800
    Entao nenhum evento de XP ou ranking e disparado para o sistema de gamificacao
