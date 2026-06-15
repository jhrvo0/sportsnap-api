package com.sportsnap.gamification.dominio.desafio;

import java.util.List;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/** Porta de persistencia das Insignias (RN28, RN35). */
public interface InsigniaRepositorio {

    Insignia salvar(Insignia insignia);

    List<Insignia> listarPorAtleta(AtletaId atletaId);

    /** Idempotencia: indica se o atleta ja recebeu a insignia do desafio (RN28). */
    boolean existePorAtletaEDesafio(AtletaId atletaId, int desafioId);

    void limpar();
}
