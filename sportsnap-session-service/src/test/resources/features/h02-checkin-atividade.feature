# language: pt

Funcionalidade: H02 - Gerenciar Check-in e Registro de Atividade

  Contexto:
    Dado que o sistema de Sessoes esta limpo

  Cenario: Atleta realiza CheckIn em Sessao ativa
    Dado que existe uma Sessao ativa
    Quando o Atleta 1 realiza CheckIn na Sessao
    Entao o CheckIn e registrado com sucesso
    E um evento CheckInRealizadoEvento e publicado

  Cenario: CheckIn em Sessao encerrada e rejeitado
    Dado que existe uma Sessao ja encerrada
    Quando o Atleta 2 tenta realizar CheckIn
    Entao o CheckIn e rejeitado com mensagem "Sessao encerrada"

  Cenario: Atleta nao pode realizar CheckIn duplicado na mesma Sessao
    Dado que existe uma Sessao ativa
    E o Atleta 3 ja realizou CheckIn nesta Sessao
    Quando o Atleta 3 tenta realizar CheckIn novamente
    Entao o CheckIn e rejeitado

  Cenario: Atleta registra atividade com intensidade alta
    Dado que existe uma Sessao ativa
    E o Atleta 1 realizou CheckIn na Sessao
    Quando o Atleta registra atividade com distancia "5.0", duracao 1800 e intensidade "alta"
    Entao o XP calculado e "15.0"
    E um evento AtividadeRegistradaEvento e publicado

  Cenario: XP varia conforme intensidade
    Dado que existe uma Sessao ativa
    E o Atleta 1 realizou CheckIn na Sessao
    Quando o Atleta registra atividade com distancia "10.0", duracao 3600 e intensidade "media"
    Entao o XP calculado e "20.0"

  Cenario: Cancelar CheckIn sem atividade registrada
    Dado que existe uma Sessao ativa
    E o Atleta 1 realizou CheckIn na Sessao
    Quando cancelo o CheckIn
    Entao o CheckIn fica marcado como cancelado
    E um evento CheckInCanceladoEvento e publicado

  Cenario: Cancelar CheckIn com atividade registrada e rejeitado
    Dado que existe uma Sessao ativa
    E o Atleta 1 realizou CheckIn na Sessao
    E o Atleta registrou atividade com distancia "2.0", duracao 600 e intensidade "baixa"
    Quando tento cancelar o CheckIn
    Entao o cancelamento e rejeitado

  Cenario: Listar CheckIns do Atleta
    Dado que existe uma Sessao ativa
    E o Atleta 1 realizou CheckIn na Sessao
    Quando consulto os CheckIns do Atleta 1
    Entao recebo 1 CheckIn

  Cenario: Motor de Match lista atletas com CheckIn na Sessao
    Dado que existe uma Sessao ativa
    E o Atleta 1 realizou CheckIn na Sessao
    E o Atleta 2 realizou CheckIn na Sessao
    Quando solicito a lista de atletas matchados na Sessao
    Entao recebo 2 atletas matchados
