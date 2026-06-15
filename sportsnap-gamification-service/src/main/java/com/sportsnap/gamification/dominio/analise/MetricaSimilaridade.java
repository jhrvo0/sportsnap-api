package com.sportsnap.gamification.dominio.analise;

/**
 * Estrategia (padrao Strategy) de distancia entre vetores de atributos, usada na
 * analise de similaridade (RN5). Permite trocar a metrica sem alterar o servico.
 */
public interface MetricaSimilaridade {

    double distancia(double[] a, double[] b);
}
