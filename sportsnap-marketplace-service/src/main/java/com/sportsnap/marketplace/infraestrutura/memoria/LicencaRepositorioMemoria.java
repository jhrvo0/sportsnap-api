package com.sportsnap.marketplace.infraestrutura.memoria;

import com.sportsnap.marketplace.dominio.atleta.AtletaId;
import com.sportsnap.marketplace.dominio.foto.FotoId;
import com.sportsnap.marketplace.dominio.licenca.LicencaDeImagem;
import com.sportsnap.marketplace.dominio.licenca.LicencaId;
import com.sportsnap.marketplace.dominio.licenca.LicencaRepositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// @Repository (desativado: usando JPA)
public class LicencaRepositorioMemoria implements LicencaRepositorio {

    private final Map<Integer, LicencaDeImagem> armazem = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public LicencaDeImagem salvar(LicencaDeImagem licenca) {
        if (licenca.getId() == null) {
            int novoId = sequencia.getAndIncrement();
            var nova = new LicencaDeImagem(new LicencaId(novoId), licenca.getAtletaId(),
                                             licenca.getFotoId(), licenca.getPreco(),
                                             licenca.getAdquiridaEm(), licenca.isCancelada());
            armazem.put(novoId, nova);
            return nova;
        }
        armazem.put(licenca.getId().getId(), licenca);
        return licenca;
    }

    @Override
    public Optional<LicencaDeImagem> obter(LicencaId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(armazem.get(id.getId()));
    }

    @Override
    public List<LicencaDeImagem> listarPorAtleta(AtletaId atletaId) {
        return armazem.values().stream()
            .filter(l -> l.getAtletaId().equals(atletaId))
            .collect(Collectors.toList());
    }

    @Override
    public List<LicencaDeImagem> listarPorFoto(FotoId fotoId) {
        return armazem.values().stream()
            .filter(l -> l.getFotoId().equals(fotoId))
            .collect(Collectors.toList());
    }

    @Override
    public List<LicencaDeImagem> listarTodas() {
        return new ArrayList<>(armazem.values());
    }

    @Override
    public void limpar() {
        armazem.clear();
        sequencia.set(1);
    }
}
