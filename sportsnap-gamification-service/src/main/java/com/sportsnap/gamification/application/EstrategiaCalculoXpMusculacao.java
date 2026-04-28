package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.usecases.EstrategiaCalculoXp;
import org.springframework.stereotype.Component;

// Pattern: Strategy — Estrategia de calculo de XP para musculacao
@Component
public class EstrategiaCalculoXpMusculacao implements EstrategiaCalculoXp {

    @Override
    public Double calcularXp(Double distancia, Long duracaoSegundos, Double intensidade) {
        // Musculacao prioriza intensidade e duracao
        return (intensidade * 15) + (duracaoSegundos / 60.0 * 3);
    }

    @Override
    public String getEsporte() {
        return "MUSCULACAO";
    }
}
