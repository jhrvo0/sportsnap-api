package com.sportsnap.gamification.bdd.steps;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.conexao.ConexaoRepositorio;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico;
import com.sportsnap.gamification.dominio.conexao.ConexaoServico.ConexaoCriadaEvento;
import com.sportsnap.gamification.dominio.conexao.PedidoConexaoRepositorio;
import com.sportsnap.gamification.dominio.perfil.PerfilRepositorio;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

public class ConexaoSteps {

    @Autowired private PerfilRepositorio perfilRepositorio;
    @Autowired private ConexaoServico conexaoServico;
    @Autowired private ConexaoRepositorio conexaoRepositorio;
    @Autowired private PedidoConexaoRepositorio pedidoRepositorio;
    @Autowired private ColetorDeEventos coletorDeEventos;
    @Autowired private SocialComumSteps comumSteps;

    private Exception excecao;

    @E("o Usuario {int} bloqueou o Usuario {int}")
    public void bloquear(Integer bloqueadorId, Integer bloqueadoId) {
        conexaoServico.bloquear(comumSteps.idPerfil(bloqueadorId), comumSteps.idPerfil(bloqueadoId));
    }

    @Quando("o Usuario {int} segue o Usuario {int}")
    public void seguir(Integer seguidorId, Integer seguidoId) {
        conexaoServico.seguir(comumSteps.idPerfil(seguidorId), comumSteps.idPerfil(seguidoId));
    }

    @Quando("o Usuario {int} tenta seguir a si mesmo")
    public void tentaSeguirASiMesmo(Integer usuarioId) {
        try {
            var id = comumSteps.idPerfil(usuarioId);
            conexaoServico.seguir(id, id);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Usuario {int} tenta seguir o Usuario {int}")
    public void tentaSeguir(Integer seguidorId, Integer seguidoId) {
        try {
            conexaoServico.seguir(comumSteps.idPerfil(seguidorId), comumSteps.idPerfil(seguidoId));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Usuario {int} aprova o pedido do Usuario {int}")
    public void aprovarPedido(Integer alvoId, Integer solicitanteId) {
        var pedido = pedidoRepositorio.listarPendentesPorAlvo(comumSteps.idPerfil(alvoId)).get(0);
        conexaoServico.aprovar(pedido.getId());
    }

    @Quando("o Usuario {int} recusa o pedido do Usuario {int}")
    public void recusarPedido(Integer alvoId, Integer solicitanteId) {
        var pedido = pedidoRepositorio.listarPendentesPorAlvo(comumSteps.idPerfil(alvoId)).get(0);
        conexaoServico.recusar(pedido.getId());
    }

    @Quando("o Usuario {int} deixa de seguir o Usuario {int}")
    public void deixarDeSeguir(Integer seguidorId, Integer seguidoId) {
        conexaoServico.deixarDeSeguir(comumSteps.idPerfil(seguidorId), comumSteps.idPerfil(seguidoId));
    }

    @Quando("o Usuario {int} bloqueia o Usuario {int}")
    public void bloquearUsuario(Integer bloqueadorId, Integer bloqueadoId) {
        conexaoServico.bloquear(comumSteps.idPerfil(bloqueadorId), comumSteps.idPerfil(bloqueadoId));
    }

    @Entao("existe conexao do Usuario {int} para o Usuario {int}")
    public void existeConexao(Integer seguidorId, Integer seguidoId) {
        assertTrue(conexaoRepositorio.obterPorPar(
            comumSteps.idPerfil(seguidorId), comumSteps.idPerfil(seguidoId)).isPresent());
    }

    @Entao("nao existe conexao do Usuario {int} para o Usuario {int}")
    public void naoExisteConexao(Integer seguidorId, Integer seguidoId) {
        assertTrue(conexaoRepositorio.obterPorPar(
            comumSteps.idPerfil(seguidorId), comumSteps.idPerfil(seguidoId)).isEmpty());
    }

    @E("existe pedido pendente do Usuario {int} para o Usuario {int}")
    public void existePedidoPendente(Integer solicitanteId, Integer alvoId) {
        assertTrue(pedidoRepositorio.obterPendentePorPar(
            comumSteps.idPerfil(solicitanteId), comumSteps.idPerfil(alvoId)).isPresent());
    }

    @Entao("nao existe pedido pendente do Usuario {int} para o Usuario {int}")
    public void naoExistePedidoPendente(Integer solicitanteId, Integer alvoId) {
        assertTrue(pedidoRepositorio.obterPendentePorPar(
            comumSteps.idPerfil(solicitanteId), comumSteps.idPerfil(alvoId)).isEmpty());
    }

    @E("o contador de seguidores do Usuario {int} e {int}")
    public void contadorSeguidores(Integer usuarioId, Integer esperado) {
        var perfil = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
        assertEquals(esperado, perfil.getTotalSeguidores());
    }

    @E("o contador de seguindo do Usuario {int} e {int}")
    public void contadorSeguindo(Integer usuarioId, Integer esperado) {
        var perfil = perfilRepositorio.obterPorUsuario(new AtletaId(usuarioId)).orElseThrow();
        assertEquals(esperado, perfil.getTotalSeguindo());
    }

    @E("um evento ConexaoCriadaEvento e publicado")
    public void eventoConexaoCriada() {
        assertTrue(coletorDeEventos.getEventos().stream().anyMatch(e -> e instanceof ConexaoCriadaEvento));
    }

    @Entao("o seguimento e rejeitado")
    public void seguimentoRejeitado() {
        assertNotNull(excecao);
    }
}
