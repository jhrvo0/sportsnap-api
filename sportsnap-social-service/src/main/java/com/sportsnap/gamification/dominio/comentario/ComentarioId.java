package com.sportsnap.gamification.dominio.comentario;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class ComentarioId {

    private final int id;

    public ComentarioId(int id) {
        isTrue(id > 0, "O id do Comentario deve ser positivo");
        this.id = id;
    }

    public int getId() { return id; }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ComentarioId outro && id == outro.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return Integer.toString(id); }
}
