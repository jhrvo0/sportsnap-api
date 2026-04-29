package com.sportsnap.session.application;

import com.sportsnap.session.domain.entities.RegistroDeAtividade;
import com.sportsnap.session.domain.repositories.RegistroDeAtividadeRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryRegistroDeAtividadeRepository implements RegistroDeAtividadeRepository {

    private final Map<Long, RegistroDeAtividade> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public RegistroDeAtividade save(RegistroDeAtividade registro) {
        if (registro.getId() == null) {
            registro.setId(sequence.getAndIncrement());
        }
        store.put(registro.getId(), registro);
        return registro;
    }

    @Override
    public Optional<RegistroDeAtividade> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<RegistroDeAtividade> findByCheckInId(Long checkInId) {
        return store.values().stream()
                .filter(r -> r.getCheckIn() != null && checkInId.equals(r.getCheckIn().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
