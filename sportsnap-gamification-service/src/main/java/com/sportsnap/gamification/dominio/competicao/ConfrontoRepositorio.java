package com.sportsnap.gamification.dominio.competicao;

import java.util.List;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/** Porta de persistencia dos Confrontos (RN44). */
public interface ConfrontoRepositorio {

    Confronto salvar(Confronto confronto);

    List<Confronto> listarPorAtleta(AtletaId atletaId);

    void limpar();
}
