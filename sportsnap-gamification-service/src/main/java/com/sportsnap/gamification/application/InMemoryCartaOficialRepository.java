package com.sportsnap.gamification.application;

import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.repositories.CartaOficialRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryCartaOficialRepository implements CartaOficialRepository {

    private final Map<Long, CartaOficial> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public CartaOficial save(CartaOficial cartaOficial) {
        if (cartaOficial.getId() == null) {
            cartaOficial.setId(sequence.getAndIncrement());
        }
        store.put(cartaOficial.getId(), cartaOficial);
        return cartaOficial;
    }

    @Override
    public Optional<CartaOficial> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<CartaOficial> findByAtletaId(Long atletaId) {
        return store.values().stream()
                .filter(c -> c.getAtleta() != null && c.getAtleta().getId().equals(atletaId))
                .findFirst();
    }

    @Override
    public List<CartaOficial> findAllByOrderByOverallDesc() {
        return store.values().stream()
                .sorted(Comparator.comparingDouble(CartaOficial::getOverall).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
