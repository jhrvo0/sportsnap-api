package com.sportsnap.session.dominio.spot;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class SpotId {

    private final int id;

    public SpotId(int id) {
        isTrue(id > 0, "O id do Spot deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SpotId) {
            var outro = (SpotId) obj;
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
