package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.usecases.EstrategiaCalculoXp;
import org.springframework.stereotype.Component;

// Pattern: Strategy — Estrategia de calculo de XP para corrida
@Component
public class EstrategiaCalculoXpCorrida implements EstrategiaCalculoXp {

    @Override
    public Double calcularXp(Double distancia, Long duracaoSegundos, Double intensidade) {
        // Corrida prioriza distancia e ritmo (distancia/duracao)
        double ritmo = duracaoSegundos > 0 ? distancia / (duracaoSegundos / 60.0) : 0;
        return (distancia * 10) + (ritmo * 5) + (intensidade * 2);
    }

    @Override
    public String getEsporte() {
        return "CORRIDA";
    }
}
