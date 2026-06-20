# language: pt

Funcionalidade: H11 - Feed de Atividades e Curtidas

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Feed retorna apenas itens de perfis seguidos
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 3 ja possui Perfil publico "Ana Costa"
    E o Usuario 1 segue o Usuario 2
    E o Usuario 3 publicou um item no feed
    Quando o Usuario 1 consulta o feed pagina 0
    Entao o feed do Usuario 1 esta vazio

  Cenario: Feed contem item publicado por seguido
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    E o Usuario 2 publicou um item no feed
    Quando o Usuario 1 consulta o feed pagina 0
    Entao o feed do Usuario 1 tem 1 item

  Cenario: Curtida idempotente nao duplica
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    E o Usuario 2 publicou um item no feed
    Quando o Usuario 1 curte o item do feed
    E o Usuario 1 curte o item do feed novamente
    Entao o numero de curtidas do item e 1

  Cenario: Descurtir remove a curtida
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    E o Usuario 2 publicou um item no feed
    E o Usuario 1 curtiu o item do feed
    Quando o Usuario 1 descurte o item do feed
    Entao o numero de curtidas do item e 0

  Cenario: Feed nao mostra itens de perfis bloqueados
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    E o Usuario 2 publicou um item no feed
    Quando o Usuario 1 bloqueia o Usuario 2
    E o Usuario 1 consulta o feed pagina 0
    Entao o feed do Usuario 1 esta vazio

  Cenario: Item recente recebe bonus de pontuacao
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    E o Usuario 2 ja possui Perfil publico "Joao Silva"
    E o Usuario 1 segue o Usuario 2
    E o Usuario 2 publicou um item recente no feed
    Quando consulto a pontuacao do item do Usuario 2 sem curtidas
    Entao a pontuacao e maior que zero
