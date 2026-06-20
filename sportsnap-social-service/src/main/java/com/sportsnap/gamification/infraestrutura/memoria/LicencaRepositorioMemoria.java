package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.sincronizacao.Licenca;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

// @Repository (desativado: usando JPA)
public class LicencaRepositorioMemoria implements LicencaRepositorio {

    private final List<Licenca> armazem = new CopyOnWriteArrayList<>();

    @Override
    public void registrar(Licenca licenca) {
        armazem.add(licenca);
    }

    @Override
    public List<Licenca> listarPorAtleta(AtletaId atletaId) {
        return armazem.stream()
            .filter(l -> l.getAtletaId().equals(atletaId))
            .collect(Collectors.toList());
    }

    @Override
    public boolean existeLicencaPosterior(AtletaId atletaId, LocalDateTime instante) {
        return armazem.stream()
            .anyMatch(l -> l.getAtletaId().equals(atletaId) && l.getAdquiridaEm().isAfter(instante));
    }

    @Override
    public void limpar() {
        armazem.clear();
    }
}
