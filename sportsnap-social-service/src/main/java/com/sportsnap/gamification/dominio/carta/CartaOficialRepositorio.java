package com.sportsnap.gamification.dominio.carta;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

import java.util.List;
import java.util.Optional;

public interface CartaOficialRepositorio {

    CartaOficial salvar(CartaOficial carta);

    Optional<CartaOficial> obterPorAtleta(AtletaId atletaId);

    List<CartaOficial> listarTodas();

    List<CartaOficial> listarSincronizadasOrdenadasPorOverall();

    void limpar();
}
