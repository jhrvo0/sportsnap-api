package com.sportsnap.gamification.dominio.mensagem;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class MensagemId {

    private final int id;

    public MensagemId(int id) {
        isTrue(id > 0, "O id da Mensagem deve ser positivo");
        this.id = id;
    }

    public int getId() { return id; }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MensagemId outro && id == outro.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return Integer.toString(id); }
}
