package com.sportsnap.gamification.dominio.post;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.feed.ItemFeed;
import com.sportsnap.gamification.dominio.feed.TipoItemFeed;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.infraestrutura.memoria.*;
import com.sportsnap.gamification.infraestrutura.evento.EventoBarramentoMemoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostEsportivoServicoTest {

    private final PostEsportivoRepositorioMemoria repositorio = new PostEsportivoRepositorioMemoria();
    private PostEsportivoServico servico;
    private FeedServico feedServico;

    @BeforeEach
    void setUp() {
        feedServico = new FeedServico(
            new ItemFeedRepositorioMemoria(),
            new CurtidaRepositorioMemoria(),
            new ConexaoRepositorioMemoria(),
            new BloqueioRepositorioMemoria(),
            new EventoBarramentoMemoria()
        );
        servico = new PostEsportivoServico(repositorio, feedServico);
    }

    @Test
    void criarPostPublicaNoFeed() {
        var autorId = new PerfilId(1);

        var post = servico.criar(autorId, "Treino de corrida hoje!", "CORRIDA");

        assertNotNull(post);
        assertEquals("Treino de corrida hoje!", post.getConteudo());
        assertEquals("CORRIDA", post.getEsporte());

        var itensFeed = feedServico.listarPorAutor(autorId);
        assertFalse(itensFeed.isEmpty());
    }

    @Test
    void criarPostComAutorNulo() {
        assertThrows(NullPointerException.class,
            () -> servico.criar(null, "Conteudo", "CORRIDA"));
    }

    @Test
    void obterPostExistente() {
        var autorId = new PerfilId(1);
        var post = servico.criar(autorId, "Treino", "CORRIDA");

        var obtido = servico.obter(post.getId());

        assertEquals(post.getId(), obtido.getId());
    }

    @Test
    void obterPostInexistente() {
        var id = new PostEsportivoId(999);
        assertThrows(IllegalArgumentException.class, () -> servico.obter(id));
    }

    @Test
    void listarPostsPorAutor() {
        var autorId = new PerfilId(1);
        servico.criar(autorId, "Post 1", "CORRIDA");
        servico.criar(autorId, "Post 2", "MUSCULACAO");

        var posts = servico.listarPorAutor(autorId);

        assertEquals(2, posts.size());
    }
}
