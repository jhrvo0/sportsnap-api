package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// @Repository (desativado: usando JPA)
public class StatusPotencialRepositorioMemoria implements StatusPotencialRepositorio {

    private final Map<Integer, StatusPotencial> armazem = new ConcurrentHashMap<>();

    @Override
    public StatusPotencial salvar(StatusPotencial status) {
        armazem.put(status.getAtletaId().getId(), status);
        return status;
    }

    @Override
    public Optional<StatusPotencial> obterPorAtleta(AtletaId atletaId) {
        if (atletaId == null) return Optional.empty();
        return Optional.ofNullable(armazem.get(atletaId.getId()));
    }

    @Override
    public void limpar() {
        armazem.clear();
    }
}
