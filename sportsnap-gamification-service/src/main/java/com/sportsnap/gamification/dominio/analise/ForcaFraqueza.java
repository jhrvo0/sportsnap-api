package com.sportsnap.gamification.dominio.analise;

/** Classificacao de um atributo do atleta frente a media da modalidade (RN8). */
public class ForcaFraqueza {

    private final String atributo;
    private final double valor;
    private final double media;
    private final ClassificacaoForca classificacao;

    public ForcaFraqueza(String atributo, double valor, double media, ClassificacaoForca classificacao) {
        this.atributo = atributo;
        this.valor = valor;
        this.media = media;
        this.classificacao = classificacao;
    }

    public String getAtributo() {
        return atributo;
    }

    public double getValor() {
        return valor;
    }

    public double getMedia() {
        return media;
    }

    public ClassificacaoForca getClassificacao() {
        return classificacao;
    }
}
