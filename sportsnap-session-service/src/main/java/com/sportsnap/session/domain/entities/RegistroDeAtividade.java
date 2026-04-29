package com.sportsnap.session.domain.entities;

public class RegistroDeAtividade {

    private Long id;
    private Double distancia;
    private Long duracaoSegundos;
    private String intensidade;
    private CheckIn checkIn;
    private double xpCalculado;

    public RegistroDeAtividade() {}

    public RegistroDeAtividade(Double distancia, Long duracaoSegundos, String intensidade, CheckIn checkIn) {
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

    public String getIntensidade() { return intensidade; }
    public void setIntensidade(String intensidade) { this.intensidade = intensidade; }

    public CheckIn getCheckIn() { return checkIn; }
    public void setCheckIn(CheckIn checkIn) { this.checkIn = checkIn; }

    public double getXpCalculado() { return xpCalculado; }
    public void setXpCalculado(double xpCalculado) { this.xpCalculado = xpCalculado; }
}
