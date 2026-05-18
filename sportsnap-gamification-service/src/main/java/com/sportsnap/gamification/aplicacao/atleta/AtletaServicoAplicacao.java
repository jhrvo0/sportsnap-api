package com.sportsnap.gamification.aplicacao.atleta;

import java.util.List;

public class AtletaServicoAplicacao {

    private final AtletaRepositorioAplicacao repositorio;

    public AtletaServicoAplicacao(AtletaRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }

    public List<AtletaResumo> pesquisarResumos() {
        return repositorio.pesquisarResumos();
    }
}
