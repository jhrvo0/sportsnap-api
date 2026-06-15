package com.sportsnap.gamification.dominio.analise;

/** Distancia euclidiana entre vetores de atributos (RN5). */
public class DistanciaEuclidiana implements MetricaSimilaridade {

    @Override
    public double distancia(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vetores de dimensoes diferentes");
        }
        double soma = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            soma += d * d;
        }
        return Math.sqrt(soma);
    }
}
