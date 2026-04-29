package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Lote;
import com.sportsnap.marketplace.domain.repositories.LoteRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryLoteRepository implements LoteRepository {

    private final Map<Long, Lote> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Lote save(Lote lote) {
        if (lote.getId() == null) {
            lote.setId(sequence.getAndIncrement());
        }
        store.put(lote.getId(), lote);
        return lote;
    }

    @Override
    public Optional<Lote> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Lote> findBySessionId(Long sessionId) {
        return store.values().stream()
                .filter(l -> l.getSessionId() != null && l.getSessionId().equals(sessionId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Lote> findByFotografoId(Long fotografoId) {
        return store.values().stream()
                .filter(l -> l.getFotografo() != null && l.getFotografo().getId() != null
                        && l.getFotografo().getId().equals(fotografoId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
