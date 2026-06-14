package com.sportsnap.session.dominio.sessao;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class SessaoId {

    private final int id;

    public SessaoId(int id) {
        isTrue(id > 0, "O id da Sessao deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SessaoId) {
            var outra = (SessaoId) obj;
            return id == outra.id;
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
