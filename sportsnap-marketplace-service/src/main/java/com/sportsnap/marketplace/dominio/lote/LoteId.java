package com.sportsnap.marketplace.dominio.lote;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class LoteId {

    private final int id;

    public LoteId(int id) {
        isTrue(id > 0, "O id do Lote deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LoteId outro && id == outro.id;
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
