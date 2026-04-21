package com.sportsnap.marketplace.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fotos")
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String urlPreview;

    private String urlOriginal;

    private LocalDateTime timestampExif;

    @Column(columnDefinition = "TEXT")
    private String metadadosExif;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @OneToMany(mappedBy = "foto", cascade = CascadeType.ALL)
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

    public Long getVersion() { return version; }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    public List<LicencaDeImagem> getLicencas() { return licencas; }
    public void setLicencas(List<LicencaDeImagem> licencas) { this.licencas = licencas; }
}
