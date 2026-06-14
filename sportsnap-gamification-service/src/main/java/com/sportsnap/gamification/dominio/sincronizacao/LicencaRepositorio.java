package com.sportsnap.gamification.dominio.sincronizacao;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

import java.time.LocalDateTime;
import java.util.List;

public interface LicencaRepositorio {

    void registrar(Licenca licenca);

    List<Licenca> listarPorAtleta(AtletaId atletaId);

    boolean existeLicencaPosterior(AtletaId atletaId, LocalDateTime instante);

    void limpar();
}
