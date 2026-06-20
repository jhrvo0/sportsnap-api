package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.comentario.ComentarioId;
import com.sportsnap.gamification.dominio.comentario.ComentarioRepositorio;
import com.sportsnap.gamification.dominio.comentario.ComentarioServico;
import com.sportsnap.gamification.dominio.feed.FeedServico;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import com.sportsnap.gamification.dominio.post.PostEsportivo;
import com.sportsnap.gamification.dominio.post.PostEsportivoRepositorio;
import com.sportsnap.gamification.dominio.post.PostEsportivoServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PostEsportivoSteps {

    @Autowired private PostEsportivoServico postEsportivoServico;
    @Autowired private PostEsportivoRepositorio postEsportivoRepositorio;
    @Autowired private ComentarioServico comentarioServico;
    @Autowired private ComentarioRepositorio comentarioRepositorio;
    @Autowired private FeedServico feedServico;

    private PostEsportivo postAtual;
    private com.sportsnap.gamification.dominio.comentario.Comentario comentarioAtual;
    private List<com.sportsnap.gamification.dominio.comentario.Comentario> comentariosAtuais;
    private Exception excecao;
    private PerfilId ultimoAutorId;

    @Dado("que existe um post esportivo do Atleta {int}")
    public void existePostEsportivo(Integer atletaId) {
        ultimoAutorId = new PerfilId(atletaId);
        postAtual = postEsportivoServico.criar(ultimoAutorId, "Treino inicial", "CORRIDA");
    }

    @Dado("que existe um comentario no post do Atleta {int}")
    public void existeComentarioNoPost(Integer atletaId) {
        ultimoAutorId = new PerfilId(atletaId);
        postAtual = postEsportivoServico.criar(new PerfilId(1), "Post para comentario", "CORRIDA");
        comentarioAtual = comentarioServico.comentar(
            new com.sportsnap.gamification.dominio.feed.ItemFeedId(postAtual.getId().getId()),
            new PerfilId(2), "Comentario original");
    }

    @Dado("que existe um comentario do Atleta {int} no post")
    public void existeComentarioDoAtletaNoPost(Integer atletaId) {
        postAtual = postEsportivoServico.criar(new PerfilId(1), "Post protegido", "CORRIDA");
        comentarioAtual = comentarioServico.comentar(
            new com.sportsnap.gamification.dominio.feed.ItemFeedId(postAtual.getId().getId()),
            new PerfilId(atletaId), "Comentario do autor");
    }

    @Dado("que existem {int} comentarios no post")
    public void existemComentariosNoPost(Integer quantidade) {
        postAtual = postEsportivoServico.criar(new PerfilId(1), "Post multiplos comentarios", "CORRIDA");
        var itemId = new com.sportsnap.gamification.dominio.feed.ItemFeedId(postAtual.getId().getId());
        for (int i = 1; i <= quantidade; i++) {
            comentarioServico.comentar(itemId, new PerfilId(i), "Comentario " + i);
        }
    }

    @Quando("o Atleta {int} cria um post esportivo com conteudo {string} e esporte {string}")
    public void atletaCriaPost(Integer atletaId, String conteudo, String esporte) {
        ultimoAutorId = new PerfilId(atletaId);
        postAtual = postEsportivoServico.criar(ultimoAutorId, conteudo, esporte);
    }

    @Quando("o Atleta {int} comenta no post com conteudo {string}")
    public void atletaComentaNoPost(Integer atletaId, String conteudo) {
        var itemId = new com.sportsnap.gamification.dominio.feed.ItemFeedId(postAtual.getId().getId());
        comentarioAtual = comentarioServico.comentar(itemId, new PerfilId(atletaId), conteudo);
    }

    @Quando("o Atleta {int} responde ao comentario com conteudo {string}")
    public void atletaRespondeComentario(Integer atletaId, String conteudo) {
        var itemId = new com.sportsnap.gamification.dominio.feed.ItemFeedId(postAtual.getId().getId());
        comentarioAtual = comentarioServico.responder(itemId, new PerfilId(atletaId), conteudo, comentarioAtual.getId());
    }

    @Quando("o Atleta {int} tenta remover o comentario")
    public void atletaTentaRemoverComentario(Integer atletaId) {
        try {
            comentarioServico.remover(comentarioAtual.getId(), new PerfilId(atletaId));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("consulto os comentarios do post")
    public void consultoComentariosDoPost() {
        var itemId = new com.sportsnap.gamification.dominio.feed.ItemFeedId(postAtual.getId().getId());
        comentariosAtuais = comentarioServico.listarPorItem(itemId);
    }

    @Entao("o post e criado com sucesso")
    public void postCriadoComSucesso() {
        assertNotNull(postAtual);
        assertNotNull(postAtual.getId());
    }

    @Entao("o post aparece na listagem do Atleta {int}")
    public void postApareceNaListagem(Integer atletaId) {
        var posts = postEsportivoServico.listarPorAutor(new PerfilId(atletaId));
        assertFalse(posts.isEmpty());
        assertTrue(posts.stream().anyMatch(p -> p.getId().equals(postAtual.getId())));
    }

    @Entao("o comentario e criado com sucesso")
    public void comentarioCriadoComSucesso() {
        assertNotNull(comentarioAtual);
        assertNotNull(comentarioAtual.getId());
    }

    @Entao("o comentario aparece na listagem do post")
    public void comentarioApareceNaListagem() {
        var itemId = new com.sportsnap.gamification.dominio.feed.ItemFeedId(postAtual.getId().getId());
        var comentarios = comentarioServico.listarPorItem(itemId);
        assertTrue(comentarios.stream().anyMatch(c -> c.getId().equals(comentarioAtual.getId())));
    }

    @Entao("a resposta e criada com sucesso")
    public void respostaCriadaComSucesso() {
        assertNotNull(comentarioAtual);
        assertNotNull(comentarioAtual.getId());
    }

    @Entao("a resposta possui referencia ao comentario pai")
    public void respostaPossuiReferenciaPai() {
        assertNotNull(comentarioAtual.getParentId());
    }

    @Entao("a remocao e rejeitada")
    public void remocaoRejeitada() {
        assertNotNull(excecao, "Remocao deveria ter falhado");
    }

    @Entao("recebo {int} comentarios")
    public void receboComentarios(Integer quantidade) {
        assertEquals(quantidade, comentariosAtuais.size());
    }
}
