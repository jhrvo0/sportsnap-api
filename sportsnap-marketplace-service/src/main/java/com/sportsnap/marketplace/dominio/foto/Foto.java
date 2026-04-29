package com.sportsnap.marketplace.dominio.foto;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.marketplace.dominio.lote.LoteId;

public class Foto {

    private final FotoId id;
    private final LoteId loteId;
    private String urlPreview;
    private String urlOriginal;
    private MetadadosExif exif;
    private boolean licenciada;
    private boolean removida;

    public Foto(LoteId loteId, String urlPreview, String urlOriginal, MetadadosExif exif) {
        id = null;
        notNull(loteId, "A Foto precisa de um Lote valido");
        this.loteId = loteId;
        setUrlPreview(urlPreview);
        setUrlOriginal(urlOriginal);
        setExif(exif);
        this.licenciada = false;
        this.removida = false;
    }

    public Foto(FotoId id, LoteId loteId, String urlPreview, String urlOriginal,
                MetadadosExif exif, boolean licenciada, boolean removida) {
        notNull(id, "O id da Foto nao pode ser nulo");
        notNull(loteId, "A Foto precisa de um Lote valido");
        this.id = id;
        this.loteId = loteId;
        setUrlPreview(urlPreview);
        setUrlOriginal(urlOriginal);
        setExif(exif);
        this.licenciada = licenciada;
        this.removida = removida;
    }

    public FotoId getId() {
        return id;
    }

    public LoteId getLoteId() {
        return loteId;
    }

    public String getUrlPreview() {
        return urlPreview;
    }

    public void setUrlPreview(String urlPreview) {
        notNull(urlPreview, "A URL de preview nao pode ser nula");
        notBlank(urlPreview, "A URL de preview nao pode estar em branco");
        this.urlPreview = urlPreview;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public void setUrlOriginal(String urlOriginal) {
        notNull(urlOriginal, "A URL original nao pode ser nula");
        notBlank(urlOriginal, "A URL original nao pode estar em branco");
        this.urlOriginal = urlOriginal;
    }

    public MetadadosExif getExif() {
        return exif;
    }

    public void setExif(MetadadosExif exif) {
        notNull(exif, "Os metadados EXIF nao podem ser nulos");
        this.exif = exif;
    }

    public boolean isLicenciada() {
        return licenciada;
    }

    public void marcarLicenciada() {
        this.licenciada = true;
    }

    public boolean isRemovida() {
        return removida;
    }

    public void remover() {
        if (licenciada) {
            throw new IllegalStateException("Nao e possivel remover Foto ja licenciada");
        }
        this.removida = true;
    }
}
