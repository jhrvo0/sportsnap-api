package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.Spot;
import com.sportsnap.session.domain.repositories.SpotRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemorySpotRepository implements SpotRepository {

    private final Map<Long, Spot> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Spot save(Spot spot) {
        if (spot.getId() == null) {
            spot.setId(sequence.getAndIncrement());
        }
        store.put(spot.getId(), spot);
        return spot;
    }

    @Override
    public Optional<Spot> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Spot> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
