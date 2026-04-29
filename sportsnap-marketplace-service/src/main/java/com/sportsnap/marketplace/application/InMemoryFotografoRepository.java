package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.Fotografo;
import com.sportsnap.marketplace.domain.repositories.FotografoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryFotografoRepository implements FotografoRepository {

    private final Map<Long, Fotografo> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Fotografo save(Fotografo fotografo) {
        if (fotografo.getId() == null) {
            fotografo.setId(sequence.getAndIncrement());
        }
        store.put(fotografo.getId(), fotografo);
        return fotografo;
    }

    @Override
    public Optional<Fotografo> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Fotografo> findByEmail(String email) {
        return store.values().stream()
                .filter(f -> f.getEmail() != null && f.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public List<Fotografo> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
