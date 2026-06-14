package com.sportsnap.session.bdd.steps;

import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.Sessao;
import com.sportsnap.session.dominio.sessao.SessaoRepositorio;
import com.sportsnap.session.dominio.sessao.SessaoServico;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.Spot;
import com.sportsnap.session.dominio.spot.SpotRepositorio;
import com.sportsnap.session.dominio.spot.SpotServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciarSessaoSteps {

    @Autowired private SpotServico spotServico;
    @Autowired private SpotRepositorio spotRepositorio;
    @Autowired private SessaoServico sessaoServico;
    @Autowired private SessaoRepositorio sessaoRepositorio;

    private Spot spot;
    private Sessao sessao;
    private Exception excecao;
    private List<Sessao> sessoesConsultadas;

    @Quando("cadastro um Spot {string} com coordenadas {string} e {string}")
    public void cadastroSpotComCoordenadas(String nome, String lat, String lon) {
        try {
            spot = spotServico.cadastrar(nome,
                new Coordenada(Double.parseDouble(lat), Double.parseDouble(lon)),
                "Local de teste");
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("tento cadastrar um Spot {string} com latitude {string}")
    public void tentoCadastrarSpotInvalido(String nome, String latInvalida) {
        try {
            spotServico.cadastrar(nome, new Coordenada(Double.parseDouble(latInvalida), 0.0), "Invalido");
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o Spot e salvo no repositorio")
    public void spotFoiSalvo() {
        assertNull(excecao, () -> "Nao deveria lancar excecao: " + excecao);
        assertNotNull(spot);
        assertTrue(spotRepositorio.obter(spot.getId()).isPresent());
    }

    @E("o Spot tem um id atribuido")
    public void spotTemId() {
        assertNotNull(spot.getId());
    }

    @Entao("o cadastro do Spot e rejeitado")
    public void cadastroSpotRejeitado() {
        assertNotNull(excecao, "Deveria ter lancado excecao");
    }

    @Dado("que existe um Spot {string} cadastrado")
    public void spotCadastrado(String nome) {
        spot = spotServico.cadastrar(nome, new Coordenada(-8.0631, -34.8711), "Spot de teste");
    }

    @Quando("cadastro uma Sessao neste Spot com duracao de {int} horas")
    public void cadastroSessaoComDuracao(Integer horas) {
        LocalDateTime agora = LocalDateTime.now();
        var periodo = new Periodo(agora.minusMinutes(1), agora.plusHours(horas));
        sessao = sessaoServico.cadastrar(spot.getId(), periodo, "Sessao de teste");
    }

    @Entao("a Sessao e salva com id")
    public void sessaoSalvaComId() {
        assertNotNull(sessao);
        assertNotNull(sessao.getId());
        assertTrue(sessaoRepositorio.obter(sessao.getId()).isPresent());
    }

    @E("a Sessao aparece entre as sessoes ativas")
    public void sessaoAparece() {
        var ativas = sessaoServico.listarAtivas();
        assertTrue(ativas.stream().anyMatch(s -> s.getId().equals(sessao.getId())));
    }

    @Quando("tento cadastrar uma Sessao com fim anterior ao inicio")
    public void tentoCadastrarSessaoInvalida() {
        try {
            LocalDateTime agora = LocalDateTime.now();
            var periodo = new Periodo(agora.plusHours(1), agora);
            sessaoServico.cadastrar(spot.getId(), periodo, "Invalida");
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o cadastro da Sessao e rejeitado")
    public void cadastroSessaoRejeitado() {
        assertNotNull(excecao, "Deveria ter lancado excecao");
    }

    @E("este Spot tem {int} Sessoes cadastradas")
    public void spotTemSessoes(Integer quantidade) {
        LocalDateTime agora = LocalDateTime.now();
        for (int i = 0; i < quantidade; i++) {
            var periodo = new Periodo(agora.plusMinutes(i), agora.plusHours(2));
            sessaoServico.cadastrar(spot.getId(), periodo, "Sessao " + i);
        }
    }

    @Quando("consulto as Sessoes deste Spot")
    public void consultoSessoesDoSpot() {
        sessoesConsultadas = sessaoServico.listarPorSpot(spot.getId());
    }

    @Entao("recebo {int} Sessoes")
    public void receboQuantidadeSessoes(Integer quantidade) {
        assertEquals(quantidade, sessoesConsultadas.size());
    }

    @E("existe uma Sessao futura neste Spot")
    public void sessaoFutura() {
        LocalDateTime agora = LocalDateTime.now();
        var periodo = new Periodo(agora.plusHours(1), agora.plusHours(3));
        sessao = sessaoServico.cadastrar(spot.getId(), periodo, "Sessao futura");
    }

    @Quando("cancelo a Sessao")
    public void canceloSessao() {
        sessaoServico.cancelar(sessao.getId());
    }

    @Entao("a Sessao fica marcada como cancelada")
    public void sessaoCancelada() {
        var recuperada = sessaoServico.obter(sessao.getId());
        assertTrue(recuperada.isCancelada());
    }

    @E("a Sessao nao aparece entre as sessoes ativas")
    public void sessaoNaoAparece() {
        var ativas = sessaoServico.listarAtivas();
        assertFalse(ativas.stream().anyMatch(s -> s.getId().equals(sessao.getId())));
    }
}
