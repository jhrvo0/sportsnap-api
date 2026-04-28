# language: pt

Funcionalidade: Venda de Foto e Licença de Imagem

  Cenário: Atleta compra licença de uma foto com sucesso
    Dado que o Fotografo "Carlos" possui um Lote com fotos de uma Sessão
    E o Atleta "João" possui um CheckIn dentro do intervalo da Sessão
    E existe uma Foto com timestamp EXIF compatível com o CheckIn
    Quando o Atleta adquire a LicencaDeImagem da Foto
    Então a LicencaDeImagem é registrada com o preço correto
    E um SplitFinanceiro é gerado atomicamente
    E o crédito do Fotografo é registrado
    E a taxa da plataforma é registrada

  Cenário: Dois atletas tentam comprar a última licença simultaneamente
    Dado que existe uma Foto com apenas uma licença disponível
    E dois Atletas tentam adquirir a licença ao mesmo tempo
    Quando ambas as compras são processadas
    Então apenas uma compra é concluída com sucesso
    E a outra recebe um erro de conflito de concorrência

  Cenário: Split financeiro respeita percentuais corretos
    Dado que o Fotografo "Ana" possui um Lote com fotos de uma Sessão
    E o Atleta "Pedro" possui um CheckIn dentro do intervalo da Sessão
    E existe uma Foto com timestamp EXIF compatível com o CheckIn
    Quando o Atleta adquire a LicencaDeImagem da Foto
    Então a LicencaDeImagem é registrada com o preço correto
    E o crédito do Fotografo corresponde a 70 por cento do preço
    E a taxa da plataforma corresponde a 30 por cento do preço
