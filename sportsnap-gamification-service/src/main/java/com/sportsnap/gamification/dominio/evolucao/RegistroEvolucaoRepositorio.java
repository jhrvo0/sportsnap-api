package com.sportsnap.gamification.dominio.evolucao;

import java.util.List;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Porta de persistencia do historico de evolucao (RN24). Apenas insercao e
 * leitura: o historico e imutavel (sem atualizacao nem exclusao individual).
 */
public interface RegistroEvolucaoRepositorio {

    RegistroEvolucao inserir(RegistroEvolucao registro);

    List<RegistroEvolucao> listarPorAtleta(AtletaId atletaId);

    long contarPorAtleta(AtletaId atletaId);

    void limpar();
}
