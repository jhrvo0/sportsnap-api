package com.sportsnap.marketplace.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Foto {

    private Long id;
    private String urlPreview;
    private String urlOriginal;
    private LocalDateTime timestampExif;
    private String metadadosExif;
    private Lote lote;
    private List<LicencaDeImagem> licencas = new ArrayList<>();

    public Foto() {}

    public Foto(String urlPreview, String urlOriginal, LocalDateTime timestampExif, Lote lote) {
        this.urlPreview = urlPreview;
        this.urlOriginal = urlOriginal;
        this.timestampExif = timestampExif;
        this.lote = lote;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrlPreview() { return urlPreview; }
    public void setUrlPreview(String urlPreview) { this.urlPreview = urlPreview; }

    public String getUrlOriginal() { return urlOriginal; }
    public void setUrlOriginal(String urlOriginal) { this.urlOriginal = urlOriginal; }

    public LocalDateTime getTimestampExif() { return timestampExif; }
    public void setTimestampExif(LocalDateTime timestampExif) { this.timestampExif = timestampExif; }

    public String getMetadadosExif() { return metadadosExif; }
    public void setMetadadosExif(String metadadosExif) { this.metadadosExif = metadadosExif; }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    public List<LicencaDeImagem> getLicencas() { return licencas; }
    public void setLicencas(List<LicencaDeImagem> licencas) { this.licencas = licencas; }
}
