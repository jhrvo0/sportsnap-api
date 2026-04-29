package com.sportsnap.marketplace.dominio.fotografo;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class FotografoId {

    private final int id;

    public FotografoId(int id) {
        isTrue(id > 0, "O id do Fotografo deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FotografoId outro && id == outro.id;
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
