package com.sportsnap.marketplace.dominio.foto;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class FotoId {

    private final int id;

    public FotoId(int id) {
        isTrue(id > 0, "O id da Foto deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FotoId outro && id == outro.id;
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
