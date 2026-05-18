package com.sportsnap.gamification.dominio.xp;

public class EstrategiaXpMusculacao implements CalculoXpEstrategia {

    @Override
    public double calcular(double distancia, long duracaoSegundos) {
        return (duracaoSegundos / 60.0) * 2.0;
    }

    @Override
    public String getTipoEsporte() {
        return "MUSCULACAO";
    }
}
