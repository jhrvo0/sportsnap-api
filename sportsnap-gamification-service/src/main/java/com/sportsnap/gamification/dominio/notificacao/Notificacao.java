package com.sportsnap.gamification.dominio.notificacao;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class Notificacao {

    private final NotificacaoId id;
    private final PerfilId destinatarioId;
    private final TipoNotificacao tipo;
    private final int referenciaId;
    private boolean lida;
    private final LocalDateTime criadaEm;
    private int numAtores;

    public Notificacao(PerfilId destinatarioId, TipoNotificacao tipo, int referenciaId) {
        notNull(destinatarioId, "O destinatarioId da Notificacao nao pode ser nulo");
        notNull(tipo,           "O tipo da Notificacao nao pode ser nulo");
        this.id             = null;
        this.destinatarioId = destinatarioId;
        this.tipo           = tipo;
        this.referenciaId   = referenciaId;
        this.lida           = false;
        this.criadaEm       = LocalDateTime.now();
        this.numAtores      = 1;
    }

    public Notificacao(NotificacaoId id, PerfilId destinatarioId, TipoNotificacao tipo,
                       int referenciaId, boolean lida, LocalDateTime criadaEm, int numAtores) {
        notNull(id,             "O id da Notificacao nao pode ser nulo");
        notNull(destinatarioId, "O destinatarioId da Notificacao nao pode ser nulo");
        notNull(tipo,           "O tipo da Notificacao nao pode ser nulo");
        this.id             = id;
        this.destinatarioId = destinatarioId;
        this.tipo           = tipo;
        this.referenciaId   = referenciaId;
        this.lida           = lida;
        this.criadaEm       = criadaEm;
        this.numAtores      = numAtores;
    }

    public NotificacaoId getId() {
        return id;
    }

    public PerfilId getDestinatarioId() {
        return destinatarioId;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public int getReferenciaId() {
        return referenciaId;
    }

    public boolean isLida() {
        return lida;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public int getNumAtores() {
        return numAtores;
    }

    public void marcarComoLida() {
        this.lida = true;
    }

    public void incrementarAtores() {
        this.numAtores++;
        this.lida = false;
    }
}
