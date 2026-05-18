package com.sportsnap.marketplace.aplicacao.foto;

import java.util.List;

public class FotoServicoAplicacao {

    private final FotoRepositorioAplicacao repositorio;

    public FotoServicoAplicacao(FotoRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }

    public List<FotoResumo> pesquisarResumos() {
        return repositorio.pesquisarResumos();
    }
}
