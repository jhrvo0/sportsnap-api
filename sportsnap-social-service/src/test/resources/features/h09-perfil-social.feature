# language: pt

Funcionalidade: H09 - Perfil Social e Rede de Conexoes

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Criar perfil para atleta existente
    Dado que o Usuario 1 nao possui Perfil
    Quando o Usuario 1 cria Perfil com nome "Maria Atleta" e tipo "ATLETA"
    Entao o Perfil do Usuario 1 existe com nome "Maria Atleta"
    E o Perfil do Usuario 1 e publico por padrao

  Cenario: Nao pode criar dois perfis para o mesmo usuario
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    Quando o Usuario 1 tenta criar Perfil novamente
    Entao a criacao e rejeitada

  Cenario: Titular edita o proprio perfil
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    Quando o Usuario 1 edita bio para "Corredora de montanha"
    Entao a bio do Perfil do Usuario 1 e "Corredora de montanha"

  Cenario: Bio acima do limite e rejeitada
    Dado que o Usuario 1 ja possui Perfil publico "Maria Atleta"
    Quando o Usuario 1 tenta definir bio com 301 caracteres
    Entao a edicao e rejeitada

  Cenario: Sugestao de conexoes considera esporte em comum
    Dado que o Usuario 1 ja possui Perfil publico "Maria" com esporte "corrida"
    E o Usuario 2 ja possui Perfil publico "Joao" com esporte "corrida"
    E o Usuario 3 ja possui Perfil publico "Ana" com esporte "surf"
    Quando o Usuario 1 solicita sugestoes de conexoes
    Entao a primeira sugestao e o Perfil do Usuario 2

  Cenario: Sugestao nao inclui perfis ja seguidos
    Dado que o Usuario 1 ja possui Perfil publico "Maria" com esporte "corrida"
    E o Usuario 2 ja possui Perfil publico "Joao" com esporte "corrida"
    E o Usuario 1 segue o Usuario 2
    Quando o Usuario 1 solicita sugestoes de conexoes
    Entao a sugestao nao inclui o Perfil do Usuario 2
