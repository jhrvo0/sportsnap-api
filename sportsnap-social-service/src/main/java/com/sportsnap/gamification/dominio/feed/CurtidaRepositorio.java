package com.sportsnap.gamification.dominio.feed;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.Optional;

public interface CurtidaRepositorio {

    Curtida salvar(Curtida curtida);

    void remover(CurtidaId id);

    Optional<Curtida> obterPorPar(PerfilId usuarioId, ItemFeedId itemId);

    int contarPorItem(ItemFeedId itemId);

    void limpar();
}
