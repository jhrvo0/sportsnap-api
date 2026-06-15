package com.sportsnap.gamification.dominio.reveal;

import com.sportsnap.gamification.dominio.carta.TierCarta;

/**
 * Resultado nao-destrutivo da previa de uma alocacao do Reveal (RN19): mostra o
 * Overall e o tier resultantes, se haveria promocao, o custo e o saldo restante,
 * sem persistir nenhuma alteracao.
 */
public class Simulacao {

    private final double overallAnterior;
    private final double overallResultante;
    private final TierCarta tierAnterior;
    private final TierCarta tierResultante;
    private final int custoTotal;
    private final int saldoRestante;

    public Simulacao(double overallAnterior, double overallResultante,
                     TierCarta tierAnterior, TierCarta tierResultante,
                     int custoTotal, int saldoRestante) {
        this.overallAnterior = overallAnterior;
        this.overallResultante = overallResultante;
        this.tierAnterior = tierAnterior;
        this.tierResultante = tierResultante;
        this.custoTotal = custoTotal;
        this.saldoRestante = saldoRestante;
    }

    public double getOverallAnterior() {
        return overallAnterior;
    }

    public double getOverallResultante() {
        return overallResultante;
    }

    public TierCarta getTierAnterior() {
        return tierAnterior;
    }

    public TierCarta getTierResultante() {
        return tierResultante;
    }

    public boolean isHaveriaPromocao() {
        return tierResultante.ehSuperiorA(tierAnterior);
    }

    public int getCustoTotal() {
        return custoTotal;
    }

    public int getSaldoRestante() {
        return saldoRestante;
    }
}
