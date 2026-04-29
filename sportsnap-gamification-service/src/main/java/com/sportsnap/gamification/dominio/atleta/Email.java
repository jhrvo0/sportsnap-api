package com.sportsnap.gamification.dominio.atleta;

import static org.apache.commons.lang3.Validate.matchesPattern;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Objects;

public class Email {

    private static final String PADRAO = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final String endereco;

    public Email(String endereco) {
        notNull(endereco, "O email nao pode ser nulo");
        notBlank(endereco, "O email nao pode estar em branco");
        matchesPattern(endereco, PADRAO, "Email invalido: %s", endereco);
        this.endereco = endereco;
    }

    public String getEndereco() {
        return endereco;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Email outro && endereco.equalsIgnoreCase(outro.endereco);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endereco.toLowerCase());
    }

    @Override
    public String toString() {
        return endereco;
    }
}
