package com.sportsnap.gamification.dominio.atleta;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class Atleta {

    private final AtletaId id;
    private String nome;
    private Email email;

    public Atleta(String nome, Email email) {
        id = null;
        setNome(nome);
        setEmail(email);
    }

    public Atleta(AtletaId id, String nome, Email email) {
        notNull(id, "O id do Atleta nao pode ser nulo");
        this.id = id;
        setNome(nome);
        setEmail(email);
    }

    public AtletaId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        notNull(nome, "O nome do Atleta nao pode ser nulo");
        notBlank(nome, "O nome do Atleta nao pode estar em branco");
        this.nome = nome;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        notNull(email, "O email do Atleta nao pode ser nulo");
        this.email = email;
    }

    @Override
    public String toString() {
        return nome;
    }
}
