package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.entities.SplitFinanceiro;
import com.sportsnap.marketplace.domain.repositories.SplitFinanceiroRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemorySplitFinanceiroRepository implements SplitFinanceiroRepository {

    private final Map<Long, SplitFinanceiro> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public SplitFinanceiro save(SplitFinanceiro split) {
        if (split.getId() == null) {
            split.setId(sequence.getAndIncrement());
        }
        store.put(split.getId(), split);
        return split;
    }

    @Override
    public Optional<SplitFinanceiro> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<SplitFinanceiro> findByLicencaDeImagemId(Long licencaId) {
        return store.values().stream()
                .filter(s -> s.getLicencaDeImagem() != null && s.getLicencaDeImagem().getId() != null
                        && s.getLicencaDeImagem().getId().equals(licencaId))
                .findFirst();
    }

    @Override
    public void deleteAll() {
        store.clear();
        sequence.set(1);
    }
}
