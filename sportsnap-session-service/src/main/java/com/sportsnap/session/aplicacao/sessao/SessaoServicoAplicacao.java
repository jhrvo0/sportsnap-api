package com.sportsnap.session.aplicacao.sessao;

import java.util.List;

public class SessaoServicoAplicacao {

    private final SessaoRepositorioAplicacao repositorio;

    public SessaoServicoAplicacao(SessaoRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }

    public List<SessaoResumo> pesquisarResumos() {
        return repositorio.pesquisarResumos();
    }
}
