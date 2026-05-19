package com.sportsnap.session.aplicacao.sessao;

import com.sportsnap.session.dominio.sessao.SessaoId;

import java.util.List;

public class SessaoServicoAplicacao {

    private final SessaoRepositorioAplicacao repositorio;

    public SessaoServicoAplicacao(SessaoRepositorioAplicacao repositorio) {
        this.repositorio = repositorio;
    }

    public List<SessaoResumo> pesquisarResumos() {
        return repositorio.pesquisarResumos();
    }

    public SessaoResumo buscarResumo(SessaoId id) {
        return repositorio.buscarResumo(id);
    }
}
