package com.sportsnap.session.dominio.checkin;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class CheckInId {

    private final int id;

    public CheckInId(int id) {
        isTrue(id > 0, "O id do CheckIn deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof CheckInId) {
            var outro = (CheckInId) obj;
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
