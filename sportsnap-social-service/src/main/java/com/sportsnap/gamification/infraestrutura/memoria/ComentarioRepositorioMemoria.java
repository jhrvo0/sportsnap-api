package com.sportsnap.gamification.infraestrutura.memoria;

import com.sportsnap.gamification.dominio.comentario.Comentario;
import com.sportsnap.gamification.dominio.comentario.ComentarioId;
import com.sportsnap.gamification.dominio.comentario.ComentarioRepositorio;
import com.sportsnap.gamification.dominio.feed.ItemFeedId;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

public class ComentarioRepositorioMemoria implements ComentarioRepositorio {

    private final Map<ComentarioId, Comentario> dados = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(0);

    @Override
    public Comentario salvar(Comentario comentario) {
        if (comentario.getId() == null) {
            var novo = new Comentario(
                new ComentarioId(sequencia.incrementAndGet()),
                comentario.getItemFeedId(),
                comentario.getAutorId(),
                comentario.getConteudo(),
                comentario.getParentId(),
                comentario.getCriadoEm()
            );
            dados.put(novo.getId(), novo);
            return novo;
        }
        dados.put(comentario.getId(), comentario);
        return comentario;
    }

    @Override
    public Optional<Comentario> obter(ComentarioId id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<Comentario> listarPorItem(ItemFeedId itemFeedId) {
        return dados.values().stream()
            .filter(c -> c.getItemFeedId().equals(itemFeedId))
            .toList();
    }

    @Override
    public List<Comentario> listarRespostasPorParent(ComentarioId parentId) {
        return dados.values().stream()
            .filter(c -> parentId.equals(c.getParentId()))
            .toList();
    }

    @Override
    public void remover(ComentarioId id) {
        dados.remove(id);
    }

    @Override
    public void limpar() {
        dados.clear();
        sequencia.set(0);
    }
}
