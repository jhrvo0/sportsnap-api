package com.sportsnap.session.dominio.atividade;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class RegistroAtividadeId {

    private final int id;

    public RegistroAtividadeId(int id) {
        isTrue(id > 0, "O id do RegistroAtividade deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof RegistroAtividadeId) {
            var outro = (RegistroAtividadeId) obj;
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
