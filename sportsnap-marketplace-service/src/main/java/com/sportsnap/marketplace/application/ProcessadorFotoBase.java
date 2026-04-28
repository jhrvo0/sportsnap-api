package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.usecases.ProcessadorFoto;

// Pattern: Decorator — Processador base que apenas retorna o caminho original
public class ProcessadorFotoBase implements ProcessadorFoto {

    @Override
    public String processar(String caminhoOriginal) {
        return caminhoOriginal;
    }

    @Override
    public String getDescricao() {
        return "Foto original";
    }
}
