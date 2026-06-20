# language: pt

Funcionalidade: H13 - Gerenciar assinatura mensal de cotas

  Contexto:
    Dado que o sistema de Marketplace esta limpo

  Cenario: Atleta assina com sucesso e recebe cotas
    Quando o Atleta 1 assina o plano mensal
    Entao a assinatura e criada com status "ATIVA"
    E o atleta possui 10 cotas disponiveis

  Cenario: Atleta tenta assinar ja tendo assinatura ativa
    Dado que o Atleta 1 ja possui assinatura ativa
    Quando o Atleta 1 tenta assinar novamente
    Entao a assinatura e rejeitada

  Cenario: Atleta compra foto via cota da assinatura
    Dado que o Atleta 1 ja possui assinatura ativa
    E que existe uma Foto disponivel para assinatura
    Quando o Atleta 1 adquire a Licenca da Foto via cota
    Entao a licenca e registrada com preco "0.00"
    E a licenca esta marcada como adquirida via cota
    E o saldo de cotas do atleta e reduzido em 1

  Cenario: Atleta cancela assinatura e mantem cotas ate fim do ciclo
    Dado que o Atleta 1 ja possui assinatura ativa
    Quando o Atleta 1 cancela a assinatura
    Entao o status da assinatura e "CANCELADA_PENDENTE"
    E o atleta ainda possui cotas disponiveis

  Cenario: Fechar ciclo distribui rateio aos fotografos
    Dado que o Atleta 1 ja possui assinatura ativa
    E que existe uma Foto disponivel para assinatura
    E o Atleta 1 adquiriu a Licenca da Foto via cota
    Quando o ciclo de assinatura do Atleta 1 e fechado
    Entao um SplitFinanceiro e gerado para a licenca
    E o valor do fotografo no split e calculado com base na mensalidade

  Cenario: Rollover de cotas respeita limite maximo
    Dado que o Atleta 1 ja possui assinatura ativa
    E o atleta ajusta saldo para 25 cotas
    Quando o ciclo de assinatura do Atleta 1 e fechado
    Entao o saldo de cotas nao excede 30
