package com.sportsnap.gamification.dominio.post;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.List;

import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import com.sportsnap.gamification.dominio.perfil.PerfilId;

public class PostEsportivoServico {

    private final PostEsportivoRepositorio repositorio;
    private final FeedServico feedServico;

    public PostEsportivoServico(PostEsportivoRepositorio repositorio, FeedServico feedServico) {
        notNull(repositorio, "O repositorio de PostEsportivo nao pode ser nulo");
        notNull(feedServico, "O feedServico nao pode ser nulo");
        this.repositorio = repositorio;
        this.feedServico = feedServico;
    }

    public PostEsportivo criar(PerfilId autorId, String conteudo, String esporte) {
        notNull(autorId, "O autorId nao pode ser nulo");
        var post = repositorio.salvar(new PostEsportivo(autorId, conteudo, esporte));
        feedServico.publicarItem(autorId, TipoItemFeed.POST_ESPORTIVO, post.getId().getId());
        return post;
    }

    public PostEsportivo obter(PostEsportivoId id) {
        notNull(id, "O id nao pode ser nulo");
        return repositorio.obter(id)
            .orElseThrow(() -> new IllegalArgumentException("PostEsportivo nao encontrado: " + id));
    }

    public List<PostEsportivo> listarPorAutor(PerfilId autorId) {
        notNull(autorId, "O autorId nao pode ser nulo");
        return repositorio.listarPorAutor(autorId);
    }
}
