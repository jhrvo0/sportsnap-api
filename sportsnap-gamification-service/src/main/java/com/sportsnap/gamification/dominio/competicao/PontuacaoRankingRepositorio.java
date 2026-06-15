package com.sportsnap.gamification.dominio.competicao;

import java.util.List;
import java.util.Optional;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/** Porta de persistencia da Pontuacao de Ranking (RN28, RN33). */
public interface PontuacaoRankingRepositorio {

    PontuacaoRanking salvar(PontuacaoRanking pontuacao);

    Optional<PontuacaoRanking> obterPorAtleta(AtletaId atletaId);

    /** Todas as pontuacoes ordenadas por PR decrescente (RN33). */
    List<PontuacaoRanking> listarOrdenadasPorPr();

    void limpar();
}
