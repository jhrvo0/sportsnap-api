package com.sportsnap.gamification.dominio.competicao;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Posicao de um atleta no ranking e sua liga (RN42). Objeto de valor retornado
 * pela consulta de posicao.
 */
public class PosicaoLiga {

    private final AtletaId atletaId;
    private final int posicao;
    private final Liga liga;
    private final double pr;

    public PosicaoLiga(AtletaId atletaId, int posicao, Liga liga, double pr) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        isTrue(posicao > 0, "A posicao deve ser positiva");
        notNull(liga, "A liga nao pode ser nula");
        this.atletaId = atletaId;
        this.posicao = posicao;
        this.liga = liga;
        this.pr = pr;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public int getPosicao() {
        return posicao;
    }

    public Liga getLiga() {
        return liga;
    }

    public double getPr() {
        return pr;
    }
}
