package com.sportsnap.gamification.dominio.post;

import com.sportsnap.gamification.dominio.perfil.PerfilId;

import java.util.List;
import java.util.Optional;

public interface PostEsportivoRepositorio {

    PostEsportivo salvar(PostEsportivo post);

    Optional<PostEsportivo> obter(PostEsportivoId id);

    List<PostEsportivo> listarPorAutor(PerfilId autorId);

    void limpar();
}
