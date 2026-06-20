package com.sportsnap.gamification.dominio.feed;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;

public interface ItemFeedRepositorio {

    ItemFeed salvar(ItemFeed item);

    Optional<ItemFeed> obter(ItemFeedId id);

    List<ItemFeed> listarPorAutores(List<PerfilId> autoresIds);

    List<ItemFeed> listarPorAutor(PerfilId autorId);

    void limpar();
}
