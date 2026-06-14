# language: pt

Funcionalidade: H04 - Dashboard do Fotografo

  Contexto:
    Dado que o sistema de Marketplace esta limpo

  Cenario: Dashboard exibe contagem de lotes e fotos
    Dado que existe o Fotografo "Julia" cadastrado com 2 Lotes e 3 fotos por Lote
    Quando consulto o resumo do Fotografo "Julia"
    Entao o resumo exibe 2 Lotes
    E o resumo exibe 6 fotos

  Cenario: Dashboard exibe total de vendas
    Dado que existe o Fotografo "Pedro" cadastrado com 1 Lote e 1 foto vendida
    Quando consulto o resumo do Fotografo "Pedro"
    Entao o resumo exibe 1 venda
    E o saldo disponivel do Fotografo e "20.93"

  Cenario: Fotografo sem vendas tem metricas zeradas
    Dado que existe o Fotografo "Maria" cadastrado com 1 Lote e 1 foto
    Quando consulto o resumo do Fotografo "Maria"
    Entao o resumo exibe 0 vendas
    E o saldo disponivel do Fotografo e "0.00"

  Cenario: Dashboard ignora licencas canceladas
    Dado que existe o Fotografo "Henrique" cadastrado com 1 Lote e 1 foto vendida
    E a licenca e cancelada dentro da janela
    Quando consulto o resumo do Fotografo "Henrique"
    Entao o resumo exibe 0 vendas

  Cenario: Dashboard recusa consulta de Fotografo inexistente
    Quando tento consultar o resumo de um Fotografo inexistente
    Entao a consulta e rejeitada
