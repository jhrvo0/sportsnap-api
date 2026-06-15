package com.sportsnap.gamification.dominio.competicao;

/**
 * Divisoes do ranking competitivo, definidas por piso de Pontuacao de Ranking
 * (PR). A promocao e o rebaixamento (RN34) sao consequencia direta do PR atual:
 * a liga e sempre a maior divisao cujo piso o PR alcanca.
 */
public enum Liga {

    BRONZE(0),
    PRATA(1000),
    OURO(1500),
    DIAMANTE(2000);

    private final double pisoPr;

    Liga(double pisoPr) {
        this.pisoPr = pisoPr;
    }

    public double getPisoPr() {
        return pisoPr;
    }

    public static Liga paraPr(double pr) {
        Liga resultado = BRONZE;
        for (Liga liga : values()) {
            if (pr >= liga.pisoPr) {
                resultado = liga;
            }
        }
        return resultado;
    }
}
