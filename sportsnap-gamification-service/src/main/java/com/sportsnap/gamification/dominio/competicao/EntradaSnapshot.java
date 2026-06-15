package com.sportsnap.gamification.dominio.competicao;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Entrada imutavel do snapshot final de uma temporada (RN39): congela a posicao
 * e o PR de um atleta no momento do encerramento.
 */
public class EntradaSnapshot {

    private final AtletaId atletaId;
    private final int posicao;
    private final double pr;

    public EntradaSnapshot(AtletaId atletaId, int posicao, double pr) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        isTrue(posicao > 0, "A posicao deve ser positiva");
        this.atletaId = atletaId;
        this.posicao = posicao;
        this.pr = pr;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public int getPosicao() {
        return posicao;
    }

    public double getPr() {
        return pr;
    }
}
