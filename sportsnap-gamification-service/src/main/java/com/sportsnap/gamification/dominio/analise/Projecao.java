package com.sportsnap.gamification.dominio.analise;

/** Projecao de evolucao do Overall a partir da tendencia historica (RN11). */
public class Projecao {

    private final double overallAtual;
    private final double tendencia;
    private final double overallProjetado;

    public Projecao(double overallAtual, double tendencia, double overallProjetado) {
        this.overallAtual = overallAtual;
        this.tendencia = tendencia;
        this.overallProjetado = overallProjetado;
    }

    public double getOverallAtual() {
        return overallAtual;
    }

    public double getTendencia() {
        return tendencia;
    }

    public double getOverallProjetado() {
        return overallProjetado;
    }
}
