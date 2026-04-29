package com.sportsnap.gamification.dominio.potencial;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

public class StatusPotencial {

    private final AtletaId atletaId;
    private double xpAcumulado;
    private int streakConsistencia;
    private LocalDateTime ultimaAtividade;

    public StatusPotencial(AtletaId atletaId) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        this.atletaId = atletaId;
        this.xpAcumulado = 0;
        this.streakConsistencia = 0;
    }

    public StatusPotencial(AtletaId atletaId, double xpAcumulado, int streakConsistencia,
                           LocalDateTime ultimaAtividade) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        isTrue(xpAcumulado >= 0, "O XP acumulado nao pode ser negativo");
        isTrue(streakConsistencia >= 0, "O streak nao pode ser negativo");
        this.atletaId = atletaId;
        this.xpAcumulado = xpAcumulado;
        this.streakConsistencia = streakConsistencia;
        this.ultimaAtividade = ultimaAtividade;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public double getXpAcumulado() {
        return xpAcumulado;
    }

    public int getStreakConsistencia() {
        return streakConsistencia;
    }

    public LocalDateTime getUltimaAtividade() {
        return ultimaAtividade;
    }

    public void acumularXp(double xp) {
        isTrue(xp > 0, "O XP a acumular deve ser positivo");
        this.xpAcumulado += xp;
        var agora = LocalDateTime.now();
        if (ultimaAtividade != null && ultimaAtividade.plusHours(48).isAfter(agora)) {
            streakConsistencia++;
        } else {
            streakConsistencia = 1;
        }
        this.ultimaAtividade = agora;
    }

    public double zerar() {
        double total = xpAcumulado;
        this.xpAcumulado = 0;
        return total;
    }
}
