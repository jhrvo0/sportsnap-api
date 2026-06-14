package com.sportsnap.marketplace.dominio.sugestao;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.FotoId;

import java.util.List;

public interface FavoritoRepositorio {

    void adicionar(AtletaId atletaId, FotoId fotoId);

    void remover(AtletaId atletaId, FotoId fotoId);

    List<FotoId> listarPorAtleta(AtletaId atletaId);

    void limpar();
}
