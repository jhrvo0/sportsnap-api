# language: pt

Funcionalidade: H05 - Comprar Licenca de Foto com Split Financeiro

  Contexto:
    Dado que o sistema de Marketplace esta limpo

  Cenario: Atleta adquire licenca de foto com sucesso
    Dado que existe uma Foto disponivel
    Quando o Atleta 1 adquire a Licenca da Foto
    Entao a Licenca e registrada com preco "29.90"
    E um SplitFinanceiro e criado atomicamente
    E um evento LicencaAdquiridaEvento e publicado

  Cenario: Split financeiro respeita percentuais
    Dado que existe uma Foto disponivel
    Quando o Atleta 1 adquire a Licenca da Foto
    Entao o valor do Fotografo no Split e "20.93"
    E a taxa da plataforma no Split e "8.97"

  Cenario: Atleta nao pode adquirir licenca da mesma foto duas vezes
    Dado que existe uma Foto disponivel
    E o Atleta 1 ja adquiriu a Licenca da Foto
    Quando o Atleta 1 tenta adquirir a Licenca da Foto novamente
    Entao a compra e rejeitada

  Cenario: Dois atletas adquirem licencas distintas da mesma foto
    Dado que existe uma Foto disponivel
    Quando o Atleta 1 adquire a Licenca da Foto
    E o Atleta 2 adquire a Licenca da Foto
    Entao existem 2 Licencas para a Foto

  Cenario: Listar licencas adquiridas pelo Atleta
    Dado que existe uma Foto disponivel
    E o Atleta 1 ja adquiriu a Licenca da Foto
    Quando consulto as Licencas do Atleta 1
    Entao recebo 1 Licenca

  Cenario: Cancelar licenca dentro da janela de 7 dias
    Dado que existe uma Foto disponivel
    E o Atleta 1 ja adquiriu a Licenca da Foto
    Quando cancelo a ultima Licenca do Atleta 1
    Entao a Licenca fica marcada como cancelada
    E um evento LicencaCanceladaEvento e publicado

  Cenario: Compra de Foto removida e rejeitada
    Dado que existe uma Foto disponivel
    E a Foto foi removida pelo Fotografo
    Quando o Atleta 1 tenta adquirir a Licenca da Foto
    Entao a compra e rejeitada

  Cenario: Total gasto pelo atleta soma apenas licencas ativas
    Dado que existe uma Foto disponivel
    E o Atleta 1 ja adquiriu a Licenca da Foto
    Quando cancelo a ultima Licenca do Atleta 1
    E consulto o total gasto pelo Atleta 1
    Entao o total gasto e "0.00"
