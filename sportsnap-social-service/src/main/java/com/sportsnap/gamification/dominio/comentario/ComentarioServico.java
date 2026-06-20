package com.sportsnap.gamification.dominio.comentario;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class ComentarioServico {

    private final ComentarioRepositorio repositorio;

    public ComentarioServico(ComentarioRepositorio repositorio) {
        notNull(repositorio, "O repositorio de Comentario nao pode ser nulo");
        this.repositorio = repositorio;
    }

    public Comentario comentar(ItemFeedId itemFeedId, PerfilId autorId, String conteudo) {
        notNull(itemFeedId, "O itemFeedId nao pode ser nulo");
        notNull(autorId,    "O autorId nao pode ser nulo");
        return repositorio.salvar(new Comentario(itemFeedId, autorId, conteudo, null));
    }

    public Comentario responder(ItemFeedId itemFeedId, PerfilId autorId,
                                String conteudo, ComentarioId parentId) {
        notNull(itemFeedId, "O itemFeedId nao pode ser nulo");
        notNull(autorId,    "O autorId nao pode ser nulo");
        notNull(parentId,   "O parentId nao pode ser nulo");
        repositorio.obter(parentId)
            .orElseThrow(() -> new IllegalArgumentException("Comentario pai nao encontrado: " + parentId));
        return repositorio.salvar(new Comentario(itemFeedId, autorId, conteudo, parentId));
    }

    public List<Comentario> listarPorItem(ItemFeedId itemFeedId) {
        notNull(itemFeedId, "O itemFeedId nao pode ser nulo");
        return repositorio.listarPorItem(itemFeedId);
    }

    public void remover(ComentarioId id, PerfilId solicitanteId) {
        notNull(id, "O id nao pode ser nulo");
        var comentario = repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("Comentario nao encontrado: " + id));
        if (!comentario.getAutorId().equals(solicitanteId)) {
            throw new IllegalStateException("Apenas o autor pode remover o comentario");
        }
        repositorio.remover(id);
    }
}
