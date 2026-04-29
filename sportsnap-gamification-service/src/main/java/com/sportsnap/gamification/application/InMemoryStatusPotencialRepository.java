package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.StatusPotencial;
import com.sportsnap.gamification.domain.repositories.StatusPotencialRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryStatusPotencialRepository implements StatusPotencialRepository {

    private final Map<Long, StatusPotencial> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public StatusPotencial save(StatusPotencial statusPotencial) {
        if (statusPotencial.getId() == null) {
            statusPotencial.setId(sequence.getAndIncrement());
        }
        store.put(statusPotencial.getId(), statusPotencial);
        return statusPotencial;
    }

    @Override
    public Optional<StatusPotencial> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<StatusPotencial> findByAtletaId(Long atletaId) {
        return store.values().stream()
                .filter(s -> s.getAtleta() != null && s.getAtleta().getId().equals(atletaId))
                .findFirst();
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
