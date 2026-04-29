# language: pt

Funcionalidade: Registro e Calculo de Atividade Esportiva

  Cenario: Atleta registra atividade apos check-in
    Dado que o Atleta realizou check-in na sessao ativa
    Quando o Atleta registra atividade com distancia 5 e duracao 1800 e intensidade "alta"
    Entao o RegistroDeAtividade e criado com sucesso
    E o XP calculado e proporcional a distancia e intensidade

  Cenario: XP e calculado corretamente para diferentes intensidades
    Dado que o Atleta realizou check-in na sessao ativa
    Quando o Atleta registra atividade com distancia 10 e duracao 3600 e intensidade "media"
    Entao o XP calculado deve ser 20

  Cenario: Atividade e vinculada ao check-in correto
    Dado que o Atleta realizou check-in na sessao ativa
    Quando o Atleta registra atividade com distancia 3 e duracao 900 e intensidade "baixa"
    Entao o RegistroDeAtividade esta vinculado ao CheckIn do Atleta
