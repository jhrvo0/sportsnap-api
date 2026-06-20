package com.sportsnap.marketplace.infraestrutura.memoria;

import com.sportsnap.marketplace.dominio.assinatura.Assinatura;
import com.sportsnap.marketplace.dominio.assinatura.AssinaturaId;
import com.sportsnap.marketplace.dominio.assinatura.AssinaturaRepositorio;
import com.sportsnap.marketplace.dominio.atleta.AtletaId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AssinaturaRepositorioEmMemoria implements AssinaturaRepositorio {

    private final Map<AssinaturaId, Assinatura> dados = new ConcurrentHashMap<>();

    @Override
    public Assinatura salvar(Assinatura assinatura) {
        dados.put(assinatura.getId(), assinatura);
        return assinatura;
    }

    @Override
    public Optional<Assinatura> obterPorAtleta(AtletaId atletaId) {
        return dados.values().stream()
            .filter(a -> a.getAtletaId().equals(atletaId) && a.isAtiva())
            .findFirst();
    }

    @Override
    public void limpar() {
        dados.clear();
    }
}
