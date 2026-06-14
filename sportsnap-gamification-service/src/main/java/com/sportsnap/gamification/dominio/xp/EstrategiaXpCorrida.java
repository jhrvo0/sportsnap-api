package com.sportsnap.gamification.dominio.xp;

public class EstrategiaXpCorrida implements CalculoXpEstrategia {

    @Override
    public double calcular(double distancia, long duracaoSegundos) {
        return distancia * 10.0 + (duracaoSegundos / 60.0) * 0.5;
    }

    @Override
    public String getTipoEsporte() {
        return "CORRIDA";
    }
}
