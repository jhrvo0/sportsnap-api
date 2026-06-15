package com.sportsnap.marketplace.dominio.assinatura;

import java.util.Objects;
import java.util.UUID;

public class AssinaturaId {

    private final String id;

    public AssinaturaId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O identificador da Assinatura nao pode ser nulo ou vazio");
        }
        this.id = id;
    }

    public static AssinaturaId gerar() {
        return new AssinaturaId(UUID.randomUUID().toString());
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssinaturaId that = (AssinaturaId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
