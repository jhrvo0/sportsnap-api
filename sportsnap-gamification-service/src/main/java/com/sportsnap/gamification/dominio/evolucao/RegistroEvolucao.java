package com.sportsnap.gamification.dominio.evolucao;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Registro imutavel de uma mudanca de Overall da carta (RN24). O historico de
 * evolucao e append-only: registros nunca sao alterados nem removidos.
 */
public class RegistroEvolucao {

    private final Integer id;
    private final AtletaId atletaId;
    private final double overallAnterior;
    private final double overallNovo;
    private final LocalDateTime ocorridoEm;

    public RegistroEvolucao(AtletaId atletaId, double overallAnterior, double overallNovo,
                            LocalDateTime ocorridoEm) {
        this(null, atletaId, overallAnterior, overallNovo, ocorridoEm);
    }

    public RegistroEvolucao(Integer id, AtletaId atletaId, double overallAnterior,
                            double overallNovo, LocalDateTime ocorridoEm) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(ocorridoEm, "A data do registro nao pode ser nula");
        this.id = id;
        this.atletaId = atletaId;
        this.overallAnterior = overallAnterior;
        this.overallNovo = overallNovo;
        this.ocorridoEm = ocorridoEm;
    }

    public Integer getId() {
        return id;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public double getOverallAnterior() {
        return overallAnterior;
    }

    public double getOverallNovo() {
        return overallNovo;
    }

    public double getDelta() {
        return overallNovo - overallAnterior;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }
}
