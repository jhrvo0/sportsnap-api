# language: pt

Funcionalidade: H10 - Conexoes, Pedidos e Bloqueios

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Seguir perfil publico cria conexao imediata
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    Quando o Usuario 1 segue o Usuario 2
    Entao existe conexao do Usuario 1 para o Usuario 2
    E o contador de seguidores do Usuario 2 e 1
    E o contador de seguindo do Usuario 1 e 1
    E um evento ConexaoCriadaEvento e publicado

  Cenario: Seguir perfil privado cria pedido pendente
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil privado "Joao Silva"
    Quando o Usuario 1 segue o Usuario 2
    Entao nao existe conexao do Usuario 1 para o Usuario 2
    E existe pedido pendente do Usuario 1 para o Usuario 2

  Cenario: Aprovar pedido de conexao cria conexao e atualiza contadores
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil privado "Joao Silva"
    E o Usuario 1 enviou pedido para o Usuario 2
    Quando o Usuario 2 aprova o pedido do Usuario 1
    Entao existe conexao do Usuario 1 para o Usuario 2
    E o contador de seguidores do Usuario 2 e 1

  Cenario: Recusar pedido remove o pedido sem criar conexao
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil privado "Joao Silva"
    E o Usuario 1 enviou pedido para o Usuario 2
    Quando o Usuario 2 recusa o pedido do Usuario 1
    Entao nao existe conexao do Usuario 1 para o Usuario 2
    E nao existe pedido pendente do Usuario 1 para o Usuario 2

  Cenario: Cancelar pedido pendente pelo solicitante
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil privado "Joao Silva"
    E o Usuario 1 enviou pedido para o Usuario 2
    Quando o Usuario 1 cancela o proprio pedido
    Entao nao existe pedido pendente do Usuario 1 para o Usuario 2

  Cenario: Nao pode seguir a si mesmo
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    Quando o Usuario 1 tenta seguir a si mesmo
    Entao o seguimento e rejeitado

  Cenario: Deixar de seguir remove conexao e decrementa contadores
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    Quando o Usuario 1 deixa de seguir o Usuario 2
    Entao nao existe conexao do Usuario 1 para o Usuario 2
    E o contador de seguidores do Usuario 2 e 0

  Cenario: Bloquear desfaz conexao existente
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    Quando o Usuario 1 bloqueia o Usuario 2
    Entao nao existe conexao do Usuario 1 para o Usuario 2
    E o contador de seguidores do Usuario 2 e 0

  Cenario: Perfil bloqueado nao pode enviar pedido
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil privado "Joao Silva"
    E o Usuario 1 bloqueou o Usuario 2
    Quando o Usuario 2 tenta seguir o Usuario 1
    Entao o seguimento e rejeitado
