package com.sportsnap.gamification.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_potencial")
public class StatusPotencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double xpAcumulado;

    @Column(nullable = false)
    private Integer streakDeConsistencia;

    private LocalDateTime ultimaAtividade;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id", nullable = false, unique = true)
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
