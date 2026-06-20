package com.sportsnap.gamification.dominio.comentario;

import com.sportsnap.gamification.dominio.feed.ItemFeedId;

import java.util.List;
import java.util.Optional;

public interface ComentarioRepositorio {

    Comentario salvar(Comentario comentario);

    Optional<Comentario> obter(ComentarioId id);

    List<Comentario> listarPorItem(ItemFeedId itemFeedId);

    List<Comentario> listarRespostasPorParent(ComentarioId parentId);

    void remover(ComentarioId id);

    void limpar();
}
