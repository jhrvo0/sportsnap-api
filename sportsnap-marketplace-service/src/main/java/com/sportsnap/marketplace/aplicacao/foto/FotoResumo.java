package com.sportsnap.marketplace.aplicacao.foto;

import java.time.LocalDateTime;

public interface FotoResumo {
    int getId();
    int getLoteId();
    String getUrlPreview();
    LocalDateTime getExifTimestamp();
    boolean isLicenciada();
}
