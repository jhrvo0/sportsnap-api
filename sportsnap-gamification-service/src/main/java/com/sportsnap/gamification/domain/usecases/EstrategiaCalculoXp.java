package com.sportsnap.gamification.domain.usecases;

/**
 * Pattern: Strategy
 *
 * Interface que define a estrategia de calculo de XP.
 * Diferentes esportes podem ter diferentes formulas de calculo.
 * Exemplo: corrida prioriza distancia, musculacao prioriza intensidade.
 */
public interface EstrategiaCalculoXp {

    Double calcularXp(Double distancia, Long duracaoSegundos, Double intensidade);

    String getEsporte();
}
