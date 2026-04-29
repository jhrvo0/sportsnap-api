package com.sportsnap.session.dominio.atleta;

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
        if (obj != null && obj instanceof AtletaId) {
            var outro = (AtletaId) obj;
            return id == outro.id;
        }
        return false;
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
