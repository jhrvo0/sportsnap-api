package com.sportsnap.gamification.aplicacao.ranking;

import java.util.List;

public class RankingServicoAplicacao {

    private final RankingRepositorioAplicacao repositorio;

    public RankingServicoAplicacao(RankingRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }

    public List<CartaResumo> pesquisarRankingGlobal() {
        return repositorio.pesquisarRankingGlobal();
    }
}
