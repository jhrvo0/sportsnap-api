package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.usecases.ProcessadorFoto;

// Pattern: Decorator — Adiciona marca d'agua a foto
public class MarcaDaguaDecorator implements ProcessadorFoto {

    private final ProcessadorFoto processadorInterno;

    public MarcaDaguaDecorator(ProcessadorFoto processadorInterno) {
        this.processadorInterno = processadorInterno;
    }

    @Override
    public String processar(String caminhoOriginal) {
        String resultado = processadorInterno.processar(caminhoOriginal);
        // Simula aplicacao de marca d'agua
        return "watermark_" + resultado;
    }

    @Override
    public String getDescricao() {
        return processadorInterno.getDescricao() + " + Marca d'agua";
    }
}
