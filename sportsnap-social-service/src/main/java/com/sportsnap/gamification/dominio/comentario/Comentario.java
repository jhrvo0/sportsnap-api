package com.sportsnap.gamification.dominio.comentario;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class Comentario {

    private final ComentarioId id;
    private final ItemFeedId itemFeedId;
    private final PerfilId autorId;
    private String conteudo;
    private final ComentarioId parentId;
    private final LocalDateTime criadoEm;

    public Comentario(ItemFeedId itemFeedId, PerfilId autorId, String conteudo, ComentarioId parentId) {
        notNull(itemFeedId, "O itemFeedId do Comentario nao pode ser nulo");
        notNull(autorId,    "O autorId do Comentario nao pode ser nulo");
        notBlank(conteudo,  "O conteudo do Comentario nao pode ser vazio");
        isTrue(conteudo.length() <= 300, "O conteudo nao pode ter mais de 300 caracteres");
        this.id         = null;
        this.itemFeedId = itemFeedId;
        this.autorId    = autorId;
        this.conteudo   = conteudo;
        this.parentId   = parentId;
        this.criadoEm   = LocalDateTime.now();
    }

    public Comentario(ComentarioId id, ItemFeedId itemFeedId, PerfilId autorId,
                      String conteudo, ComentarioId parentId, LocalDateTime criadoEm) {
        notNull(id,         "O id do Comentario nao pode ser nulo");
        notNull(itemFeedId, "O itemFeedId do Comentario nao pode ser nulo");
        notNull(autorId,    "O autorId do Comentario nao pode ser nulo");
        this.id         = id;
        this.itemFeedId = itemFeedId;
        this.autorId    = autorId;
        this.conteudo   = conteudo;
        this.parentId   = parentId;
        this.criadoEm   = criadoEm;
    }

    public ComentarioId getId()      { return id; }
    public ItemFeedId getItemFeedId(){ return itemFeedId; }
    public PerfilId getAutorId()     { return autorId; }
    public String getConteudo()      { return conteudo; }
    public ComentarioId getParentId(){ return parentId; }
    public LocalDateTime getCriadoEm(){ return criadoEm; }
    public boolean isResposta()      { return parentId != null; }
}
