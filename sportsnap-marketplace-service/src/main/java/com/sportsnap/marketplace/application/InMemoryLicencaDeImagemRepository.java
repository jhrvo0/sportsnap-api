package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.LicencaDeImagem;
import com.sportsnap.marketplace.domain.repositories.LicencaDeImagemRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryLicencaDeImagemRepository implements LicencaDeImagemRepository {

    private final Map<Long, LicencaDeImagem> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public LicencaDeImagem save(LicencaDeImagem licenca) {
        if (licenca.getId() == null) {
            licenca.setId(sequence.getAndIncrement());
        }
        store.put(licenca.getId(), licenca);
        return licenca;
    }

    @Override
    public Optional<LicencaDeImagem> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<LicencaDeImagem> findByAtletaId(Long atletaId) {
        return store.values().stream()
                .filter(l -> l.getAtletaId() != null && l.getAtletaId().equals(atletaId))
                .collect(Collectors.toList());
    }

    @Override
    public List<LicencaDeImagem> findByFotoId(Long fotoId) {
        return store.values().stream()
                .filter(l -> l.getFoto() != null && l.getFoto().getId() != null
                        && l.getFoto().getId().equals(fotoId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
