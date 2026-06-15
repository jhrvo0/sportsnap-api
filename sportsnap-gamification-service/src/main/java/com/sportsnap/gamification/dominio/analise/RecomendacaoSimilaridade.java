package com.sportsnap.gamification.dominio.analise;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/** Recomendacao de atleta similar e sua distancia ao consultado (RN5). */
public class RecomendacaoSimilaridade {

    private final AtletaId atletaId;
    private final double distancia;

    public RecomendacaoSimilaridade(AtletaId atletaId, double distancia) {
        this.atletaId = atletaId;
        this.distancia = distancia;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public double getDistancia() {
        return distancia;
    }
}
