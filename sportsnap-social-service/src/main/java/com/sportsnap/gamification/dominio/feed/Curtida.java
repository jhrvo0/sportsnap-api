package com.sportsnap.gamification.dominio.feed;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class Curtida {

    private final CurtidaId id;
    private final PerfilId usuarioId;
    private final ItemFeedId itemId;

    public Curtida(PerfilId usuarioId, ItemFeedId itemId) {
        notNull(usuarioId, "O usuarioId da Curtida nao pode ser nulo");
        notNull(itemId,    "O itemId da Curtida nao pode ser nulo");
        this.id       = null;
        this.usuarioId = usuarioId;
        this.itemId    = itemId;
    }

    public Curtida(CurtidaId id, PerfilId usuarioId, ItemFeedId itemId) {
        notNull(id,        "O id da Curtida nao pode ser nulo");
        notNull(usuarioId, "O usuarioId da Curtida nao pode ser nulo");
        notNull(itemId,    "O itemId da Curtida nao pode ser nulo");
        this.id        = id;
        this.usuarioId = usuarioId;
        this.itemId    = itemId;
    }

    public CurtidaId getId() {
        return id;
    }

    public PerfilId getUsuarioId() {
        return usuarioId;
    }

    public ItemFeedId getItemId() {
        return itemId;
    }
}
