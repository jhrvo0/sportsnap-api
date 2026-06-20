package com.sportsnap.gamification.dominio.mensagem;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class Mensagem {

    private final MensagemId id;
    private final PerfilId remetenteId;
    private final PerfilId destinatarioId;
    private String conteudo;
    private boolean lida;
    private final LocalDateTime criadaEm;

    public Mensagem(PerfilId remetenteId, PerfilId destinatarioId, String conteudo) {
        notNull(remetenteId,    "O remetenteId da Mensagem nao pode ser nulo");
        notNull(destinatarioId, "O destinatarioId da Mensagem nao pode ser nulo");
        notBlank(conteudo,      "O conteudo da Mensagem nao pode ser vazio");
        isTrue(conteudo.length() <= 1000, "O conteudo nao pode ter mais de 1000 caracteres");
        isTrue(!remetenteId.equals(destinatarioId), "Nao e possivel enviar mensagem para si mesmo");
        this.id             = null;
        this.remetenteId    = remetenteId;
        this.destinatarioId = destinatarioId;
        this.conteudo       = conteudo;
        this.lida           = false;
        this.criadaEm       = LocalDateTime.now();
    }

    public Mensagem(MensagemId id, PerfilId remetenteId, PerfilId destinatarioId,
                    String conteudo, boolean lida, LocalDateTime criadaEm) {
        notNull(id,             "O id da Mensagem nao pode ser nulo");
        notNull(remetenteId,    "O remetenteId da Mensagem nao pode ser nulo");
        notNull(destinatarioId, "O destinatarioId da Mensagem nao pode ser nulo");
        this.id             = id;
        this.remetenteId    = remetenteId;
        this.destinatarioId = destinatarioId;
        this.conteudo       = conteudo;
        this.lida           = lida;
        this.criadaEm       = criadaEm;
    }

    public MensagemId getId()          { return id; }
    public PerfilId getRemetenteId()   { return remetenteId; }
    public PerfilId getDestinatarioId(){ return destinatarioId; }
    public String getConteudo()        { return conteudo; }
    public boolean isLida()            { return lida; }
    public LocalDateTime getCriadaEm() { return criadaEm; }

    public void marcarComoLida() { this.lida = true; }

    public boolean envolve(PerfilId perfilId) {
        return remetenteId.equals(perfilId) || destinatarioId.equals(perfilId);
    }

    public PerfilId outroParticipante(PerfilId meu) {
        return remetenteId.equals(meu) ? destinatarioId : remetenteId;
    }
}
