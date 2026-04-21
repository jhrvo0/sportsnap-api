package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.usecases.ProcessadorFoto;

// Pattern: Decorator — Redimensiona a foto para preview
public class ResizeDecorator implements ProcessadorFoto {

    private final ProcessadorFoto processadorInterno;

    public ResizeDecorator(ProcessadorFoto processadorInterno) {
        this.processadorInterno = processadorInterno;
    }

    @Override
    public String processar(String caminhoOriginal) {
        String resultado = processadorInterno.processar(caminhoOriginal);
        // Simula redimensionamento
        return "thumb_" + resultado;
    }

    @Override
    public String getDescricao() {
        return processadorInterno.getDescricao() + " + Resize";
    }
}
