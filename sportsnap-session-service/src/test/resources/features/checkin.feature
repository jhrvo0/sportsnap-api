# language: pt

Funcionalidade: Check-in do Atleta em uma Sessão

  Cenário: Atleta realiza check-in com sucesso
    Dado que existe um Spot "Praia de Boa Viagem" com coordenadas válidas
    E existe uma Session ativa neste Spot
    Quando o Atleta "João" realiza o CheckIn com localização próxima ao Spot
    Então o CheckIn é registrado com sucesso
    E o horário do CheckIn está dentro do intervalo da Session

  Cenário: Atleta tenta check-in fora do horário da sessão
    Dado que existe um Spot "Parque da Jaqueira" com coordenadas válidas
    E existe uma Session que já foi encerrada neste Spot
    Quando o Atleta "Maria" tenta realizar o CheckIn
    Então o CheckIn é rejeitado
    E uma mensagem de erro "Sessão encerrada" é exibida

  Cenário: Dois atletas fazem check-in na mesma sessão
    Dado que existe um Spot "Praia de Boa Viagem" com coordenadas válidas
    E existe uma Session ativa neste Spot
    Quando o Atleta "João" realiza o CheckIn com localização próxima ao Spot
    E o Atleta "Maria" realiza o CheckIn com localização próxima ao Spot
    Então ambos os CheckIns são registrados com sucesso
