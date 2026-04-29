package com.sportsnap.gamification.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartaOficial {

    private Long id;
    private Double overall;
    private String imagemUrl;
    private LocalDateTime ultimaSincronizacao;
    private Atleta atleta;
    private List<AtributoEsportivo> atributos = new ArrayList<>();

    public CartaOficial() {}

    public CartaOficial(Atleta atleta) {
        this.atleta = atleta;
        this.overall = 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getOverall() { return overall; }
    public void setOverall(Double overall) { this.overall = overall; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public LocalDateTime getUltimaSincronizacao() { return ultimaSincronizacao; }
    public void setUltimaSincronizacao(LocalDateTime ultimaSincronizacao) { this.ultimaSincronizacao = ultimaSincronizacao; }

    public Atleta getAtleta() { return atleta; }
    public void setAtleta(Atleta atleta) { this.atleta = atleta; }

    public List<AtributoEsportivo> getAtributos() { return atributos; }
    public void setAtributos(List<AtributoEsportivo> atributos) { this.atributos = atributos; }
}
