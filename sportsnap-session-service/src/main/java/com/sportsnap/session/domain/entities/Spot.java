package com.sportsnap.session.domain.entities;

public class Spot {

    private Long id;
    private String nome;
    private Double latitude;
    private Double longitude;
    private String descricao;

    public Spot() {}

    public Spot(String nome, Double latitude, Double longitude, String descricao) {
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
