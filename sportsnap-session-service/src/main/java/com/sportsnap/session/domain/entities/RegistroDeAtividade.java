package com.sportsnap.session.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "registros_de_atividade")
public class RegistroDeAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double distancia;

    private Long duracaoSegundos;

    private Double intensidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckIn checkIn;

    public RegistroDeAtividade() {}

    public RegistroDeAtividade(Double distancia, Long duracaoSegundos, Double intensidade, CheckIn checkIn) {
        this.distancia = distancia;
        this.duracaoSegundos = duracaoSegundos;
        this.intensidade = intensidade;
        this.checkIn = checkIn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getDistancia() { return distancia; }
    public void setDistancia(Double distancia) { this.distancia = distancia; }

    public Long getDuracaoSegundos() { return duracaoSegundos; }
    public void setDuracaoSegundos(Long duracaoSegundos) { this.duracaoSegundos = duracaoSegundos; }

    public Double getIntensidade() { return intensidade; }
    public void setIntensidade(Double intensidade) { this.intensidade = intensidade; }

    public CheckIn getCheckIn() { return checkIn; }
    public void setCheckIn(CheckIn checkIn) { this.checkIn = checkIn; }
}
