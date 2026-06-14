package com.sportsnap.gamification.dominio.atleta;

import static org.apache.commons.lang3.Validate.notNull;

public class AtletaServico {

    private final AtletaRepositorio repositorio;

    public AtletaServico(AtletaRepositorio repositorio) {
        notNull(repositorio, "O repositorio de Atleta nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public Atleta cadastrar(String nome, Email email) {
        return repositorio.salvar(new Atleta(nome, email));
    }

    public Atleta obter(AtletaId id) {
        notNull(id, "O id do Atleta nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Atleta nao encontrado: " + id));
    }
}
