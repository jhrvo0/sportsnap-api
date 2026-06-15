package com.sportsnap.gamification.dominio.desafio;

import java.util.List;
import java.util.Optional;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/** Porta de persistencia dos Progressos de desafio (RN22, RN23, RN35). */
public interface ProgressoDesafioRepositorio {

    ProgressoDesafio salvar(ProgressoDesafio progresso);

    Optional<ProgressoDesafio> obterPorId(int id);

    /** Progresso ativo do atleta para o desafio, se existir (RN22). */
    Optional<ProgressoDesafio> obterAtivo(AtletaId atletaId, int desafioId);

    List<ProgressoDesafio> listarPorAtleta(AtletaId atletaId);

    void limpar();
}
