package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.repositories.CheckInRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryCheckInRepository implements CheckInRepository {

    private final Map<Long, CheckIn> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public CheckIn save(CheckIn checkIn) {
        if (checkIn.getId() == null) {
            checkIn.setId(sequence.getAndIncrement());
        }
        store.put(checkIn.getId(), checkIn);
        return checkIn;
    }

    @Override
    public Optional<CheckIn> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<CheckIn> findByAtletaId(Long atletaId) {
        return store.values().stream()
                .filter(c -> atletaId.equals(c.getAtletaId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CheckIn> findBySessionId(Long sessionId) {
        return store.values().stream()
                .filter(c -> c.getSession() != null && sessionId.equals(c.getSession().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
