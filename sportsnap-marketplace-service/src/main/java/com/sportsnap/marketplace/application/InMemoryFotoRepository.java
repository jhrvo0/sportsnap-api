package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Foto;
import com.sportsnap.marketplace.domain.repositories.FotoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryFotoRepository implements FotoRepository {

    private final Map<Long, Foto> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Foto save(Foto foto) {
        if (foto.getId() == null) {
            foto.setId(sequence.getAndIncrement());
        }
        store.put(foto.getId(), foto);
        return foto;
    }

    @Override
    public Optional<Foto> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Foto> findByLoteId(Long loteId) {
        return store.values().stream()
                .filter(f -> f.getLote() != null && f.getLote().getId() != null
                        && f.getLote().getId().equals(loteId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
