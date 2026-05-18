package com.sportsnap.gamification.dominio.xp;

public interface CalculoXpEstrategia {
    double calcular(double distancia, long duracaoSegundos);
    String getTipoEsporte();
}
