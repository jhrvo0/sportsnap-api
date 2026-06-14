package com.sportsnap.marketplace.dominio.fotografo;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class FotografoServico {

    private final FotografoRepositorio repositorio;

    public FotografoServico(FotografoRepositorio repositorio) {
        notNull(repositorio, "O repositorio de Fotografo nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public Fotografo cadastrar(String nome, Email email) {
        return repositorio.salvar(new Fotografo(nome, email));
    }

    public Fotografo obter(FotografoId id) {
        notNull(id, "O id do Fotografo nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Fotografo nao encontrado: " + id));
    }

    public List<Fotografo> listarTodos() {
        return repositorio.listarTodos();
    }
}
