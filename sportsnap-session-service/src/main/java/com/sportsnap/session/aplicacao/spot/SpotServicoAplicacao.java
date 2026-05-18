package com.sportsnap.session.aplicacao.spot;

import java.util.List;

public class SpotServicoAplicacao {

    private final SpotRepositorioAplicacao repositorio;

    public SpotServicoAplicacao(SpotRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }

    public List<SpotResumo> pesquisarResumos() {
        return repositorio.pesquisarResumos();
    }
}
