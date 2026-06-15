package com.sportsnap.gamification.dominio.competicao;

/**
 * Calculo estilo Elo (RN31): vencer um oponente mais forte rende mais PR; perder
 * para um mais fraco custa mais. Usa o resultado esperado classico do Elo com
 * fator K fixo.
 */
public class EloPadrao implements CalculoEloEstrategia {

    private static final double K = 32.0;

    @Override
    public double variacao(double prVencedor, double prPerdedor) {
        double esperadoVencedor = 1.0 / (1.0 + Math.pow(10.0, (prPerdedor - prVencedor) / 400.0));
        return K * (1.0 - esperadoVencedor);
    }
}
