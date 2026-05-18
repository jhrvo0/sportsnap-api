package com.sportsnap.marketplace.dominio.foto;

import static org.apache.commons.lang3.Validate.notNull;

public abstract class FotoDecorador implements FotoVisualizavel {

    protected final FotoVisualizavel componente;

    protected FotoDecorador(FotoVisualizavel componente) {
        notNull(componente, "O componente decorado nao pode ser nulo");
        this.componente = componente;
    }

    @Override
    public String getUrlPreview() {
        return componente.getUrlPreview();
    }

    @Override
    public String getDescricaoPreview() {
        return componente.getDescricaoPreview();
    }
}
