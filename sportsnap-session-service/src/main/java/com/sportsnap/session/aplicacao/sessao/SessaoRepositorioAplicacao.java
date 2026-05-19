package com.sportsnap.session.aplicacao.sessao;

import com.sportsnap.session.dominio.sessao.SessaoId;

import java.util.List;

public interface SessaoRepositorioAplicacao {
    List<SessaoResumo> pesquisarResumos();
    SessaoResumo buscarResumo(SessaoId id);
}
