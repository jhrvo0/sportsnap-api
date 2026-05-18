package com.sportsnap.gamification.aplicacao.ranking;

import java.time.LocalDateTime;

public interface CartaResumo {
    int getAtletaId();
    double getOverall();
    LocalDateTime getUltimaSincronizacao();

    default boolean isSincronizada() {
        return getUltimaSincronizacao() != null;
    }
}
