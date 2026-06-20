# language: pt

Funcionalidade: H13 - Mensagens Diretas entre Usuarios

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Enviar mensagem entre dois usuarios
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    Quando o Usuario 1 envia mensagem para o Usuario 2 com texto "Oi Joao, tudo bem?"
    Entao a conversa entre os Usuarios 1 e 2 tem 1 mensagem
    E a mensagem e do remetente 1 para o destinatario 2

  Cenario: Responder uma mensagem
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 enviou mensagem para o Usuario 2 com texto "Ola!"
    Quando o Usuario 2 envia mensagem para o Usuario 1 com texto "Oi! Tudo bem sim!"
    Entao a conversa entre os Usuarios 1 e 2 tem 2 mensagens

  Cenario: Mensagem nao lida gera contador
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 enviou mensagem para o Usuario 2 com texto "Mensagem nao lida"
    Quando consulto mensagens nao lidas do Usuario 2
    Entao o contador de nao lidas e 1

  Cenario: Marcar mensagem como lida zera contador
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 enviou mensagem para o Usuario 2 com texto "Mensagem nao lida"
    Quando o Usuario 2 marca a mensagem como lida
    Entao o contador de nao lidas e 0

  Cenario: Nao e possivel enviar mensagem para si mesmo
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    Quando o Usuario 1 tenta enviar mensagem para si mesmo
    Entao o envio e rejeitado

  Cenario: Inbox lista ultima mensagem de cada conversa
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 3 ja possui Perfil publico "Ana Costa"
    E o Usuario 2 enviou mensagem para o Usuario 1 com texto "Oi Maria!"
    E o Usuario 3 enviou mensagem para o Usuario 1 com texto "Ola Maria!"
    Quando consulto o inbox do Usuario 1
    Entao o inbox tem 2 conversas
