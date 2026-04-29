# language: pt

Funcionalidade: Upload e Indexacao de Fotos em Lote

  Cenario: Fotografo faz upload de fotos com sucesso
    Dado que o Fotografo "Ricardo" possui um Lote cadastrado
    Quando o Fotografo faz upload de 3 fotos no Lote
    Entao as 3 fotos sao registradas no Lote
    E cada foto possui metadados EXIF extraidos

  Cenario: Metadados EXIF sao extraidos de cada foto
    Dado que o Fotografo "Ana" possui um Lote cadastrado
    Quando o Fotografo faz upload de 1 fotos no Lote
    Entao a foto possui timestamp EXIF preenchido
    E a foto possui URL de preview gerada

  Cenario: Lote vazio nao processa upload
    Dado que o Fotografo "Carlos" possui um Lote cadastrado
    Quando o Fotografo tenta fazer upload de 0 fotos no Lote
    Entao nenhuma foto e adicionada ao Lote
