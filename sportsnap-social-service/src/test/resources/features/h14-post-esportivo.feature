# language: pt

Funcionalidade: H14 - Criar e interagir com posts esportivos

  Contexto:
    Dado que o sistema de Gamification esta limpo

  Cenario: Atleta cria post esportivo e aparece no feed
    Quando o Atleta 1 cria um post esportivo com conteudo "Treino de corrida hoje!" e esporte "CORRIDA"
    Entao o post e criado com sucesso
    E o post aparece na listagem do Atleta 1

  Cenario: Atleta comenta em post esportivo
    Dado que existe um post esportivo do Atleta 1
    Quando o Atleta 2 comenta no post com conteudo "Excelente treino!"
    Entao o comentario e criado com sucesso
    E o comentario aparece na listagem do post

  Cenario: Atleta responde comentario existente
    Dado que existe um comentario no post do Atleta 1
    Quando o Atleta 1 responde ao comentario com conteudo "Obrigado!"
    Entao a resposta e criada com sucesso
    E a resposta possui referencia ao comentario pai

  Cenario: Apenas o autor pode remover seu comentario
    Dado que existe um comentario do Atleta 2 no post
    Quando o Atleta 1 tenta remover o comentario
    Entao a remocao e rejeitada

  Cenario: Listar comentarios de um post
    Dado que existem 3 comentarios no post
    Quando consulto os comentarios do post
    Entao recebo 3 comentarios
