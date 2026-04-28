package com.sportsnap.marketplace.domain.usecases;

public interface ProcessarVendaFoto {

    void executar(Long atletaId, Long fotoId);
}
