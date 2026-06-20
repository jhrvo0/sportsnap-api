package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.feed.*;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

public class ItemFeedRepositorioMemoria implements ItemFeedRepositorio {

    private final Map<ItemFeedId, ItemFeed> dados = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(0);

    @Override
    public ItemFeed salvar(ItemFeed item) {
        if (item.getId() == null) {
            var novo = new ItemFeed(item.getAutorId(), item.getTipo(), item.getReferenciaId());
            dados.put(new ItemFeedId(sequencia.incrementAndGet()), novo);
            return novo;
        }
        dados.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<ItemFeed> obter(ItemFeedId id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<ItemFeed> listarPorAutores(List<PerfilId> autoresIds) {
        return dados.values().stream()
            .filter(item -> autoresIds.contains(item.getAutorId()))
            .toList();
    }

    @Override
    public List<ItemFeed> listarPorAutor(PerfilId autorId) {
        return dados.values().stream()
            .filter(item -> item.getAutorId().equals(autorId))
            .toList();
    }

    @Override
    public void limpar() {
        dados.clear();
        sequencia.set(0);
    }
}
