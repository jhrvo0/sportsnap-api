package com.sportsnap.gamification.dominio.competicao;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Registro imutavel de um confronto resolvido (RN29 a RN31, RN40). Guarda
 * vencedor, perdedor, a variacao de PR transferida, o instante e a temporada
 * vigente em que ocorreu.
 */
public class Confronto {

    private final Integer id;
    private final AtletaId vencedorId;
    private final AtletaId perdedorId;
    private final double prTransferida;
    private final LocalDateTime ocorridoEm;
    private final int temporadaId;

    public Confronto(AtletaId vencedorId, AtletaId perdedorId, double prTransferida,
                     LocalDateTime ocorridoEm, int temporadaId) {
        this(null, vencedorId, perdedorId, prTransferida, ocorridoEm, temporadaId);
    }

    public Confronto(Integer id, AtletaId vencedorId, AtletaId perdedorId, double prTransferida,
                     LocalDateTime ocorridoEm, int temporadaId) {
        notNull(vencedorId, "O id do vencedor nao pode ser nulo");
        notNull(perdedorId, "O id do perdedor nao pode ser nulo");
        notNull(ocorridoEm, "A data do confronto nao pode ser nula");
        this.id = id;
        this.vencedorId = vencedorId;
        this.perdedorId = perdedorId;
        this.prTransferida = prTransferida;
        this.ocorridoEm = ocorridoEm;
        this.temporadaId = temporadaId;
    }

    public Integer getId() {
        return id;
    }

    public AtletaId getVencedorId() {
        return vencedorId;
    }

    public AtletaId getPerdedorId() {
        return perdedorId;
    }

    public double getPrTransferida() {
        return prTransferida;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }

    public int getTemporadaId() {
        return temporadaId;
    }

    public boolean envolve(AtletaId atletaId) {
        return vencedorId.equals(atletaId) || perdedorId.equals(atletaId);
    }
}
