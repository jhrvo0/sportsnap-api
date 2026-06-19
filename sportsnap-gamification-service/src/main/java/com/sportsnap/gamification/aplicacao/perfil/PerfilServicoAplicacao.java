package com.sportsnap.gamification.aplicacao.perfil;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class PerfilServicoAplicacao {

    private final PerfilRepositorioAplicacao repositorio;

    public PerfilServicoAplicacao(PerfilRepositorioAplicacao repositorio) {
        notNull(repositorio, "O repositorio de PerfilAplicacao nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public List<PerfilResumo> pesquisarResumos() {
        return repositorio.pesquisarResumos();
    }
}
