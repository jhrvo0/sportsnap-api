package com.sportsnap.session.dominio.spot;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

public class SpotServico {

    private final SpotRepositorio repositorio;

    public SpotServico(SpotRepositorio repositorio) {
        notNull(repositorio, "O repositorio de Spot nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public Spot cadastrar(String nome, Coordenada coordenada, String descricao) {
        var spot = new Spot(nome, coordenada, descricao);
        return repositorio.salvar(spot);
    }

    public Spot obter(SpotId id) {
        notNull(id, "O id do Spot nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Spot nao encontrado: " + id));
    }

    public List<Spot> listarTodos() {
        return repositorio.listarTodos();
    }

    public Spot atualizar(SpotId id, String nome, Coordenada coordenada, String descricao) {
        notNull(id, "O id do Spot nao pode ser nulo");
        var existente = obter(id);
        var atualizado = new Spot(existente.getId(), nome, coordenada, descricao);
        return repositorio.salvar(atualizado);
    }

    public void remover(SpotId id) {
        notNull(id, "O id do Spot nao pode ser nulo");
        repositorio.remover(id);
    }
}
