package com.sportsnap.gamification.dominio.reveal;

import java.util.List;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/** Porta de persistencia dos Registros de Sincronizacao (RN23). */
public interface RegistroSincronizacaoRepositorio {

    RegistroSincronizacao salvar(RegistroSincronizacao registro);

    List<RegistroSincronizacao> listarPorAtleta(AtletaId atletaId);

    void limpar();
}
