# language: pt

Funcionalidade: Dashboard de Controle e Conversao do Fotografo

  Cenario: Fotografo visualiza resumo de seus lotes
    Dado que o Fotografo "Julia" possui 2 lotes com fotos
    Quando o Fotografo consulta o Dashboard
    Entao o Dashboard exibe o total de lotes como 2
    E o Dashboard exibe o total de fotos

  Cenario: Dashboard mostra total de vendas
    Dado que o Fotografo "Pedro" possui um lote com uma foto vendida
    Quando o Fotografo consulta o Dashboard
    Entao o Dashboard exibe 1 venda realizada

  Cenario: Fotografo sem vendas ve dashboard zerado
    Dado que o Fotografo "Maria" possui 1 lote com fotos sem vendas
    Quando o Fotografo consulta o Dashboard
    Entao o Dashboard exibe 0 vendas realizadas
