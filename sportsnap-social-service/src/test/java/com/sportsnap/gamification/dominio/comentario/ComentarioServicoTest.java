package com.sportsnap.gamification.dominio.comentario;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.feed.ItemFeedId;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.infraestrutura.memoria.ComentarioRepositorioMemoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComentarioServicoTest {

    private final ComentarioRepositorioMemoria repositorio = new ComentarioRepositorioMemoria();
    private ComentarioServico servico;

    @BeforeEach
    void setUp() {
        servico = new ComentarioServico(repositorio);
    }

    @Test
    void comentarComSucesso() {
        var itemId = new ItemFeedId(1);
        var autorId = new PerfilId(1);

        var comentario = servico.comentar(itemId, autorId, "Excelente treino!");

        assertNotNull(comentario);
        assertEquals("Excelente treino!", comentario.getConteudo());
        assertNull(comentario.getParentId());
    }

    @Test
    void responderAComentarioExistente() {
        var itemId = new ItemFeedId(1);
        var autorId = new PerfilId(1);
        var comentarioPai = servico.comentar(itemId, autorId, "Comentario pai");

        var resposta = servico.responder(itemId, autorId, "Resposta", comentarioPai.getId());

        assertNotNull(resposta);
        assertEquals(comentarioPai.getId(), resposta.getParentId());
    }

    @Test
    void responderAComentarioInexistente() {
        var itemId = new ItemFeedId(1);
        var autorId = new PerfilId(1);
        var parentId = new ComentarioId(999);

        assertThrows(IllegalArgumentException.class,
            () -> servico.responder(itemId, autorId, "Resposta", parentId));
    }

    @Test
    void listarComentariosPorItem() {
        var itemId = new ItemFeedId(1);
        var autorId = new PerfilId(1);
        servico.comentar(itemId, autorId, "Comentario 1");
        servico.comentar(itemId, autorId, "Comentario 2");

        var comentarios = servico.listarPorItem(itemId);

        assertEquals(2, comentarios.size());
    }

    @Test
    void removerPeloAutor() {
        var itemId = new ItemFeedId(1);
        var autorId = new PerfilId(1);
        var comentario = servico.comentar(itemId, autorId, "Para remover");

        servico.remover(comentario.getId(), autorId);

        assertTrue(repositorio.obter(comentario.getId()).isEmpty());
    }

    @Test
    void removerPorNaoAutor() {
        var itemId = new ItemFeedId(1);
        var autorId = new PerfilId(1);
        var outroAutor = new PerfilId(2);
        var comentario = servico.comentar(itemId, autorId, "Comentario protegido");

        var erro = assertThrows(IllegalStateException.class,
            () -> servico.remover(comentario.getId(), outroAutor));
        assertTrue(erro.getMessage().contains("Apenas o autor pode remover"));
    }
}
