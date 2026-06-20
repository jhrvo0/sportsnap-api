package com.sportsnap.gamification.dominio.atleta;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class AtletaId {

    private final int id;

    public AtletaId(int id) {
        isTrue(id > 0, "O id do Atleta deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AtletaId outro && id == outro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
