package com.sportsnap.marketplace.dominio.foto;

public class FotoPreviewBasico implements FotoVisualizavel {

    private final Foto foto;

    public FotoPreviewBasico(Foto foto) {
        this.foto = foto;
    }

    @Override
    public String getUrlPreview() {
        return foto.getUrlPreview();
    }

    @Override
    public String getDescricaoPreview() {
        return foto.getExif().getDetalhes();
    }
}
