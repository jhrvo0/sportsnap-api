package com.sportsnap.marketplace.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Lote {

    private Long id;
    private Long sessionId;
    private Long spotId;
    private LocalDateTime criadoEm;
    private Fotografo fotografo;
    private List<Foto> fotos = new ArrayList<>();

    public Lote() {}

    public Lote(Long sessionId, Long spotId, Fotografo fotografo) {
        this.sessionId = sessionId;
        this.spotId = spotId;
        this.fotografo = fotografo;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getSpotId() { return spotId; }
    public void setSpotId(Long spotId) { this.spotId = spotId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public Fotografo getFotografo() { return fotografo; }
    public void setFotografo(Fotografo fotografo) { this.fotografo = fotografo; }

    public List<Foto> getFotos() { return fotos; }
    public void setFotos(List<Foto> fotos) { this.fotos = fotos; }
}
