package com.sportsnap.marketplace.dominio.fotografo;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class Fotografo {

    private final FotografoId id;
    private String nome;
    private Email email;

    public Fotografo(String nome, Email email) {
        id = null;
        setNome(nome);
        setEmail(email);
    }

    public Fotografo(FotografoId id, String nome, Email email) {
        notNull(id, "O id do Fotografo nao pode ser nulo");
        this.id = id;
        setNome(nome);
        setEmail(email);
    }

    public FotografoId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        notNull(nome, "O nome do Fotografo nao pode ser nulo");
        notBlank(nome, "O nome do Fotografo nao pode estar em branco");
        this.nome = nome;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        notNull(email, "O email do Fotografo nao pode ser nulo");
        this.email = email;
    }

    @Override
    public String toString() {
        return nome;
    }
}
