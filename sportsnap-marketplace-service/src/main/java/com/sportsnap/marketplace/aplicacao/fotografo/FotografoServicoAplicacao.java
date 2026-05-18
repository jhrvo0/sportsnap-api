package com.sportsnap.marketplace.aplicacao.fotografo;

import java.util.List;

public class FotografoServicoAplicacao {

    private final FotografoRepositorioAplicacao repositorio;

    public FotografoServicoAplicacao(FotografoRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }

    public List<FotografoResumo> pesquisarResumos() {
        return repositorio.pesquisarResumos();
    }
}
