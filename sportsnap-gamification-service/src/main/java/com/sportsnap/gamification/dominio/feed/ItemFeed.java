package com.sportsnap.gamification.dominio.feed;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class ItemFeed {

    private static final double W_ENG      = 1.0;
    private static final double W_REL      = 2.0;
    private static final double LAMBDA     = 0.05;
    private static final double W_RECENCIA = 3.0;
    private static final long   HORAS_RECENCIA = 2;

    private final ItemFeedId id;
    private final PerfilId autorId;
    private final TipoItemFeed tipo;
    private final int referenciaId;
    private final LocalDateTime publicadoEm;

    public ItemFeed(PerfilId autorId, TipoItemFeed tipo, int referenciaId) {
        notNull(autorId, "O autorId do ItemFeed nao pode ser nulo");
        notNull(tipo,    "O tipo do ItemFeed nao pode ser nulo");
        this.id          = null;
        this.autorId     = autorId;
        this.tipo        = tipo;
        this.referenciaId = referenciaId;
        this.publicadoEm = LocalDateTime.now();
    }

    public ItemFeed(ItemFeedId id, PerfilId autorId, TipoItemFeed tipo,
                    int referenciaId, LocalDateTime publicadoEm) {
        notNull(id,          "O id do ItemFeed nao pode ser nulo");
        notNull(autorId,     "O autorId do ItemFeed nao pode ser nulo");
        notNull(tipo,        "O tipo do ItemFeed nao pode ser nulo");
        notNull(publicadoEm, "O publicadoEm do ItemFeed nao pode ser nulo");
        this.id           = id;
        this.autorId      = autorId;
        this.tipo         = tipo;
        this.referenciaId = referenciaId;
        this.publicadoEm  = publicadoEm;
    }

    public ItemFeedId getId() {
        return id;
    }

    public PerfilId getAutorId() {
        return autorId;
    }

    public TipoItemFeed getTipo() {
        return tipo;
    }

    public int getReferenciaId() {
        return referenciaId;
    }

    public LocalDateTime getPublicadoEm() {
        return publicadoEm;
    }

    public double calcularPontuacao(int curtidas, double grauConexao) {
        long horasDesdePublicacao = ChronoUnit.HOURS.between(publicadoEm, LocalDateTime.now());
        double decaimento = Math.exp(-LAMBDA * horasDesdePublicacao);
        double pontuacao  = curtidas * W_ENG + grauConexao * W_REL * decaimento;
        if (horasDesdePublicacao < HORAS_RECENCIA) {
            pontuacao += W_RECENCIA;
        }
        return pontuacao;
    }
}
