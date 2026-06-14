package com.sportsnap.session.dominio.spot;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class Spot {

    private final SpotId id;
    private String nome;
    private Coordenada coordenada;
    private String descricao;

    public Spot(String nome, Coordenada coordenada, String descricao) {
        id = null;
        setNome(nome);
        setCoordenada(coordenada);
        setDescricao(descricao);
    }

    public Spot(SpotId id, String nome, Coordenada coordenada, String descricao) {
        notNull(id, "O id do Spot nao pode ser nulo");
        this.id = id;
        setNome(nome);
        setCoordenada(coordenada);
        setDescricao(descricao);
    }

    public SpotId getId() {
        return id;
    }

    public void setNome(String nome) {
        notNull(nome, "O nome do Spot nao pode ser nulo");
        notBlank(nome, "O nome do Spot nao pode estar em branco");
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setCoordenada(Coordenada coordenada) {
        notNull(coordenada, "A coordenada do Spot nao pode ser nula");
        this.coordenada = coordenada;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return nome;
    }
}
