package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.feed.Curtida;
import com.sportsnap.gamification.dominio.feed.CurtidaId;
import com.sportsnap.gamification.dominio.feed.CurtidaRepositorio;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

public class CurtidaRepositorioMemoria implements CurtidaRepositorio {

    private final Map<CurtidaId, Curtida> dados = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(0);

    @Override
    public Curtida salvar(Curtida curtida) {
        var novo = new Curtida(new CurtidaId(sequencia.incrementAndGet()),
            curtida.getUsuarioId(), curtida.getItemId());
        dados.put(novo.getId(), novo);
        return novo;
    }

    @Override
    public void remover(CurtidaId id) {
        dados.remove(id);
    }

    @Override
    public Optional<Curtida> obterPorPar(PerfilId usuarioId, ItemFeedId itemId) {
        return dados.values().stream()
            .filter(c -> c.getUsuarioId().equals(usuarioId) && c.getItemId().equals(itemId))
            .findFirst();
    }

    @Override
    public int contarPorItem(ItemFeedId itemId) {
        return (int) dados.values().stream()
            .filter(c -> c.getItemId().equals(itemId))
            .count();
    }

    @Override
    public void limpar() {
        dados.clear();
        sequencia.set(0);
    }
}
