package com.sportsnap.marketplace.dominio.foto;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

public class MetadadosExif {

    private final LocalDateTime timestamp;
    private final String detalhes;

    public MetadadosExif(LocalDateTime timestamp, String detalhes) {
        notNull(timestamp, "O timestamp EXIF nao pode ser nulo");
        notNull(detalhes, "Os detalhes EXIF nao podem ser nulos");
        notBlank(detalhes, "Os detalhes EXIF nao podem estar em branco");
        this.timestamp = timestamp;
        this.detalhes = detalhes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDetalhes() {
        return detalhes;
    }

    @Override
    public String toString() {
        return detalhes;
    }
}
