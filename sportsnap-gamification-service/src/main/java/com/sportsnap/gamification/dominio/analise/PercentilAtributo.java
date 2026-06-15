package com.sportsnap.gamification.dominio.analise;

/** Posicao percentual de um atributo na base elegivel, entre 0 e 100 (RN2, RN4). */
public class PercentilAtributo {

    private final String atributo;
    private final double valor;
    private final double percentil;

    public PercentilAtributo(String atributo, double valor, double percentil) {
        this.atributo = atributo;
        this.valor = valor;
        this.percentil = percentil;
    }

    public String getAtributo() {
        return atributo;
    }

    public double getValor() {
        return valor;
    }

    public double getPercentil() {
        return percentil;
    }
}
