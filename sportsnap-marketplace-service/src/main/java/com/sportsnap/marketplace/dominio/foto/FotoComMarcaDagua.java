package com.sportsnap.marketplace.dominio.foto;

public class FotoComMarcaDagua extends FotoDecorador {

    private static final String MARCA = "© SportSnap";

    public FotoComMarcaDagua(FotoVisualizavel componente) {
        super(componente);
    }

    @Override
    public String getUrlPreview() {
        return componente.getUrlPreview() + "?marca=" + MARCA.replace(" ", "+");
    }

    @Override
    public String getDescricaoPreview() {
        return componente.getDescricaoPreview() + " [" + MARCA + "]";
    }
}
