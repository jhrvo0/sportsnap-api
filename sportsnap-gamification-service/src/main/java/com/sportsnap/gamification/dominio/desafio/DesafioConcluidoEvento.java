package com.sportsnap.gamification.dominio.desafio;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/** Evento de dominio publicado quando um desafio e concluido (RN29). */
public class DesafioConcluidoEvento {

    private final AtletaId atletaId;
    private final int desafioId;
    private final String insigniaCodigo;

    public DesafioConcluidoEvento(AtletaId atletaId, int desafioId, String insigniaCodigo) {
        this.atletaId = atletaId;
        this.desafioId = desafioId;
        this.insigniaCodigo = insigniaCodigo;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public int getDesafioId() {
        return desafioId;
    }

    public String getInsigniaCodigo() {
        return insigniaCodigo;
    }
}
