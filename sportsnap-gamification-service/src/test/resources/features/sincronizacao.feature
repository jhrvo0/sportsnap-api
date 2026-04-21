# language: pt

Funcionalidade: Sincronização da Carta do Atleta

  Cenário: Atleta sincroniza carta com licença válida
    Dado que o Atleta "João" possui um CheckIn registrado hoje
    E possui uma LicencaDeImagem adquirida após o último Reveal
    Quando o Atleta dispara a Sincronizacao
    Então os StatusPotencial são transferidos para a CartaOficial
    E o Overall é recalculado
    E a posição no Ranking é atualizada

  Cenário: Atleta tenta sincronizar sem licença válida
    Dado que o Atleta "Maria" possui um CheckIn registrado hoje
    E não possui uma LicencaDeImagem válida
    Quando o Atleta tenta disparar a Sincronizacao
    Então a sincronização é rejeitada
    E a CartaOficial permanece inalterada
