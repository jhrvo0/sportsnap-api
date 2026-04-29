package com.sportsnap.gamification.domain.entities;

import java.time.LocalDateTime;

public class StatusPotencial {

    private Long id;
    private Double xpAcumulado;
    private Integer streakDeConsistencia;
    private LocalDateTime ultimaAtividade;
    private Atleta atleta;

    public StatusPotencial() {}

    public StatusPotencial(Atleta atleta) {
        this.atleta = atleta;
        this.xpAcumulado = 0.0;
        this.streakDeConsistencia = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getXpAcumulado() { return xpAcumulado; }
    public void setXpAcumulado(Double xpAcumulado) { this.xpAcumulado = xpAcumulado; }

    public Integer getStreakDeConsistencia() { return streakDeConsistencia; }
    public void setStreakDeConsistencia(Integer streakDeConsistencia) { this.streakDeConsistencia = streakDeConsistencia; }

    public LocalDateTime getUltimaAtividade() { return ultimaAtividade; }
    public void setUltimaAtividade(LocalDateTime ultimaAtividade) { this.ultimaAtividade = ultimaAtividade; }

    public Atleta getAtleta() { return atleta; }
    public void setAtleta(Atleta atleta) { this.atleta = atleta; }
}
