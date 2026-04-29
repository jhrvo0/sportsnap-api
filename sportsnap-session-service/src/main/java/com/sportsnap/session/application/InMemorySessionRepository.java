package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.repositories.SessionRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemorySessionRepository implements SessionRepository {

    private final Map<Long, Session> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Session save(Session session) {
        if (session.getId() == null) {
            session.setId(sequence.getAndIncrement());
        }
        store.put(session.getId(), session);
        return session;
    }

    @Override
    public Optional<Session> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Session> findBySpotId(Long spotId) {
        return store.values().stream()
                .filter(s -> s.getSpot() != null && spotId.equals(s.getSpot().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
