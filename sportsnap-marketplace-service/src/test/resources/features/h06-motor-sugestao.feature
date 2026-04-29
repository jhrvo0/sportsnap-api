# language: pt

Funcionalidade: H06 - Motor de Sugestao de Fotos

  Contexto:
    Dado que o sistema de Marketplace esta limpo

  Cenario: Atleta recebe sugestoes de fotos com timestamp dentro da janela
    Dado que existe uma Foto cujo EXIF cai dentro da janela do Atleta 1
    Quando o Atleta 1 solicita sugestoes para sua janela
    Entao recebo 1 foto sugerida

  Cenario: Atleta sem janela de CheckIn nao recebe sugestoes
    Dado que existe uma Foto cujo EXIF cai dentro da janela do Atleta 1
    Quando o Atleta 1 solicita sugestoes sem janelas
    Entao recebo 0 fotos sugeridas

  Cenario: Foto com EXIF fora da janela nao e sugerida
    Dado que existe uma Foto cujo EXIF esta fora da janela do Atleta 1
    Quando o Atleta 1 solicita sugestoes para sua janela
    Entao recebo 0 fotos sugeridas

  Cenario: Foto ja adquirida nao reaparece nas sugestoes
    Dado que existe uma Foto cujo EXIF cai dentro da janela do Atleta 1
    E o Atleta 1 ja adquiriu essa Foto
    Quando o Atleta 1 solicita sugestoes para sua janela
    Entao recebo 0 fotos sugeridas

  Cenario: Favoritar e desfavoritar foto
    Dado que existe uma Foto cujo EXIF cai dentro da janela do Atleta 1
    Quando o Atleta 1 favorita a Foto
    Entao a Foto aparece nos favoritos do Atleta 1
    Quando o Atleta 1 desfavorita a Foto
    Entao a Foto nao aparece mais nos favoritos do Atleta 1
