package com.sportsnap.gamification.dominio.conexao;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Objects;

public class PedidoConexaoId {

    private final int id;

    public PedidoConexaoId(int id) {
        isTrue(id > 0, "O id do PedidoConexao deve ser positivo");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PedidoConexaoId outro && id == outro.id;
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
