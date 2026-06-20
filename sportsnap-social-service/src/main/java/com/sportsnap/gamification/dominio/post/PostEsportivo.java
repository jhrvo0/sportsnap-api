package com.sportsnap.gamification.dominio.post;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class PostEsportivo {

    private final PostEsportivoId id;
    private final PerfilId autorId;
    private String conteudo;
    private String esporte;
    private final LocalDateTime criadoEm;

    public PostEsportivo(PerfilId autorId, String conteudo, String esporte) {
        notNull(autorId, "O autorId do PostEsportivo nao pode ser nulo");
        notBlank(conteudo, "O conteudo do PostEsportivo nao pode ser vazio");
        isTrue(conteudo.length() <= 500, "O conteudo nao pode ter mais de 500 caracteres");
        this.id       = null;
        this.autorId  = autorId;
        this.conteudo = conteudo;
        this.esporte  = esporte;
        this.criadoEm = LocalDateTime.now();
    }

    public PostEsportivo(PostEsportivoId id, PerfilId autorId, String conteudo,
                         String esporte, LocalDateTime criadoEm) {
        notNull(id,     "O id do PostEsportivo nao pode ser nulo");
        notNull(autorId, "O autorId do PostEsportivo nao pode ser nulo");
        this.id       = id;
        this.autorId  = autorId;
        this.conteudo = conteudo;
        this.esporte  = esporte;
        this.criadoEm = criadoEm;
    }

    public PostEsportivoId getId()    { return id; }
    public PerfilId getAutorId()      { return autorId; }
    public String getConteudo()       { return conteudo; }
    public String getEsporte()        { return esporte; }
    public LocalDateTime getCriadoEm(){ return criadoEm; }
}
