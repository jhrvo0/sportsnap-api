# language: pt

Funcionalidade: H12 - Notificacoes

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Curtida gera notificacao para o autor do item
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    E o Usuario 2 publicou um item no feed
    Quando o Usuario 1 curte o item do feed
    Entao o Usuario 2 tem 1 notificacao nao lida do tipo CURTIDA

  Cenario: Multiplas curtidas no mesmo item sao agregadas
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 3 ja possui Perfil publico "Ana Costa"
    E o Usuario 2 publicou um item no feed
    E o Usuario 1 segue o Usuario 2
    E o Usuario 3 segue o Usuario 2
    Quando o Usuario 1 curte o item do feed
    E o Usuario 3 curte o item do feed
    Entao o Usuario 2 tem 1 notificacao nao lida do tipo CURTIDA
    E a notificacao tem 2 atores

  Cenario: Novo seguidor gera notificacao
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    Quando o Usuario 1 segue o Usuario 2
    Entao o Usuario 2 tem 1 notificacao nao lida do tipo NOVO_SEGUIDOR

  Cenario: Marcar todas como lidas zera o contador
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    Quando o Usuario 2 marca todas as notificacoes como lidas
    Entao o contador de nao lidas do Usuario 2 e 0

  Cenario: Cancelar pedido remove notificacao pendente
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil privado "Joao Silva"
    E o Usuario 1 enviou pedido para o Usuario 2
    Quando o Usuario 1 cancela o proprio pedido
    Entao o Usuario 2 nao tem notificacao de pedido de conexao
