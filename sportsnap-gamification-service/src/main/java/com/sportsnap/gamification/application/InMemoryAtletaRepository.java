package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.Atleta;
import com.sportsnap.gamification.domain.repositories.AtletaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryAtletaRepository implements AtletaRepository {

    private final Map<Long, Atleta> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Atleta save(Atleta atleta) {
        if (atleta.getId() == null) {
            atleta.setId(sequence.getAndIncrement());
        }
        store.put(atleta.getId(), atleta);
        return atleta;
    }

    @Override
    public Optional<Atleta> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Atleta> findByEmail(String email) {
        return store.values().stream()
                .filter(a -> a.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public List<Atleta> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
