package com.sportsnap.gamification.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.conexao.SugestaoConexao;
import com.sportsnap.gamification.dominio.perfil.Perfil;
import com.sportsnap.gamification.dominio.perfil.PerfilId;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilServico;
import com.sportsnap.gamification.dominio.perfil.TipoConta;
import com.sportsnap.gamification.dominio.perfil.Visibilidade;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

public class PerfilSteps {

    @Autowired private PerfilServico perfilServico;
    @Autowired private PerfilRepositorio perfilRepositorio;
    @Autowired private ConexaoServico conexaoServico;

    private Perfil perfilCriado;
    private Exception excecao;
    private List<SugestaoConexao> sugestoes;

    @Dado("que o Usuario {int} nao possui Perfil")
    public void usuarioSemPerfil(Integer usuarioId) {
        // noop — estado limpo pelo @Before
    }

    @Dado("que o Usuario {int} ja possui Perfil publico {string}")
    public void usuarioPossuiPerfilPublico(Integer usuarioId, String nome) {
        perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.ATLETA);
    }

    @Dado("que o Usuario {int} ja possui Perfil privado {string}")
    public void usuarioPossuiPerfilPrivado(Integer usuarioId, String nome) {
        var p = perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.ATLETA);
        perfilServico.editar(p.getId(), new AtletaId(usuarioId), nome, null, null, null, Visibilidade.PRIVADA, null);
    }

    @Dado("que o Usuario {int} ja possui Perfil publico {string} com esporte {string}")
    @Dado("o Usuario {int} ja possui Perfil publico {string} com esporte {string}")
    public void usuarioPossuiPerfilComEsporte(Integer usuarioId, String nome, String esporte) {
        var p = perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.ATLETA);
        perfilServico.editar(p.getId(), new AtletaId(usuarioId), nome, null, esporte, null, Visibilidade.PUBLICA, null);
    }

    @Quando("o Usuario {int} cria Perfil com nome {string} e tipo {string}")
    public void criarPerfil(Integer usuarioId, String nome, String tipo) {
        perfilCriado = perfilServico.criar(new AtletaId(usuarioId), nome, TipoConta.valueOf(tipo));
    }

    @Quando("o Usuario {int} tenta criar Perfil novamente")
    public void tentaCriarPerfilNovamente(Integer usuarioId) {
        try {
            perfilServico.criar(new AtletaId(usuarioId), "duplicado", TipoConta.ATLETA);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Usuario {int} edita bio para {string}")
    public void editarBio(Integer usuarioId, String bio) {
        var perfil = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
        perfilServico.editar(perfil.getId(), new AtletaId(usuarioId),
            perfil.getNomeExibicao(), bio, perfil.getEsporte(), perfil.getLocalidade(), null, null);
    }

    @Quando("o Usuario {int} tenta definir bio com 301 caracteres")
    public void tentaDefinirBioLonga(Integer usuarioId) {
        try {
            var perfil = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
            perfilServico.editar(perfil.getId(), new AtletaId(usuarioId),
                perfil.getNomeExibicao(), "x".repeat(301), null, null, null, null);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Usuario {int} solicita sugestoes de conexoes")
    public void solicitarSugestoes(Integer usuarioId) {
        var perfil = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
        sugestoes = conexaoServico.sugerirConexoes(perfil.getId(), 5);
    }

    @Entao("o Perfil do Usuario {int} existe com nome {string}")
    public void perfilExisteComNome(Integer usuarioId, String nome) {
        var p = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
        assertEquals(nome, p.getNomeExibicao());
    }

    @E("o Perfil do Usuario {int} e publico por padrao")
    public void perfilPublicoPorPadrao(Integer usuarioId) {
        var p = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
        assertTrue(p.isPublico());
    }

    @Entao("a criacao e rejeitada")
    public void criacaoRejeitada() {
        assertNotNull(excecao);
    }

    @Entao("a bio do Perfil do Usuario {int} e {string}")
    public void bioEsperada(Integer usuarioId, String bio) {
        var p = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
        assertEquals(bio, p.getBio());
    }

    @Entao("a edicao e rejeitada")
    public void edicaoRejeitada() {
        assertNotNull(excecao);
    }

    @Entao("a primeira sugestao e o Perfil do Usuario {int}")
    public void primeiraSugestao(Integer usuarioId) {
        assertFalse(sugestoes.isEmpty());
        var perfilEsperado = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
        assertEquals(perfilEsperado.getId(), sugestoes.get(0).getPerfil().getId());
    }

    @Entao("a sugestao nao inclui o Perfil do Usuario {int}")
    public void sugestaoNaoInclui(Integer usuarioId) {
        var perfilId = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId))
            .map(Perfil::getId).orElseThrow();
        assertTrue(sugestoes.stream().noneMatch(s -> s.getPerfil().getId().equals(perfilId)));
    }
}
