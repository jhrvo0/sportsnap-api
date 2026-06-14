package com.sportsnap.gamification.dominio.potencial;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

import java.util.Optional;

public interface StatusPotencialRepositorio {

    StatusPotencial salvar(StatusPotencial status);

    Optional<StatusPotencial> obterPorAtleta(AtletaId atletaId);

    void limpar();
}
