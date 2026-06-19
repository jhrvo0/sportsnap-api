package com.sportsnap.gamification.dominio.bloqueio;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class Bloqueio {

    private final BloqueioId id;
    private final PerfilId bloqueadorId;
    private final PerfilId bloqueadoId;

    public Bloqueio(PerfilId bloqueadorId, PerfilId bloqueadoId) {
        notNull(bloqueadorId, "O bloqueadorId do Bloqueio nao pode ser nulo");
        notNull(bloqueadoId, "O bloqueadoId do Bloqueio nao pode ser nulo");
        this.id = null;
        this.bloqueadorId = bloqueadorId;
        this.bloqueadoId = bloqueadoId;
    }

    public Bloqueio(BloqueioId id, PerfilId bloqueadorId, PerfilId bloqueadoId) {
        notNull(id, "O id do Bloqueio nao pode ser nulo");
        notNull(bloqueadorId, "O bloqueadorId do Bloqueio nao pode ser nulo");
        notNull(bloqueadoId, "O bloqueadoId do Bloqueio nao pode ser nulo");
        this.id = id;
        this.bloqueadorId = bloqueadorId;
        this.bloqueadoId = bloqueadoId;
    }

    public BloqueioId getId() {
        return id;
    }

    public PerfilId getBloqueadorId() {
        return bloqueadorId;
    }

    public PerfilId getBloqueadoId() {
        return bloqueadoId;
    }

    public boolean envolve(PerfilId perfilId) {
        return bloqueadorId.equals(perfilId) || bloqueadoId.equals(perfilId);
    }
}
