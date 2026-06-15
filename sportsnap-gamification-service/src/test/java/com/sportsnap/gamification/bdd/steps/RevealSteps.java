package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucaoRepositorio;
import com.sportsnap.gamification.dominio.reveal.Orcamento;
import com.sportsnap.gamification.dominio.reveal.RevealServico;
import com.sportsnap.gamification.dominio.reveal.Simulacao;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RevealSteps {

    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private RegistroEvolucaoRepositorio evolucaoRepositorio;
    @Autowired private RevealServico revealServico;

    private Orcamento orcamento;
    private Simulacao simulacao;
    private Exception excecao;

    @Dado("que o Atleta {int} possui CartaOficial Bronze com atributo {string} valor {int}")
    public void cartaBronzeComAtributo(Integer atletaId, String nome, Integer valor) {
        var carta = new CartaOficial(new AtletaId(atletaId),
            List.of(new AtributoEsportivo(nome, valor.doubleValue(), 1.0, "corrida")));
        cartaRepositorio.salvar(carta);
    }

    @E("a Carta do Atleta {int} esta arquivada")
    public void cartaArquivada(Integer atletaId) {
        var carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        carta.arquivar();
        cartaRepositorio.salvar(carta);
    }

    @Quando("o Atleta {int} inicia o Reveal")
    public void iniciaReveal(Integer atletaId) {
        orcamento = revealServico.iniciar(new AtletaId(atletaId));
    }

    @Entao("o orcamento liberado e {int}")
    public void orcamentoLiberado(Integer esperado) {
        assertNotNull(orcamento);
        assertEquals(esperado.intValue(), orcamento.getPontosDisponiveis());
    }

    @Quando("o Atleta {int} confirma o Reveal alocando {int} pontos em {string}")
    public void confirmaReveal(Integer atletaId, Integer pontos, String atributo) {
        try {
            revealServico.confirmar(new AtletaId(atletaId), Map.of(atributo, pontos));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Atleta {int} simula o Reveal alocando {int} pontos em {string}")
    public void simulaReveal(Integer atletaId, Integer pontos, String atributo) {
        simulacao = revealServico.simular(new AtletaId(atletaId), Map.of(atributo, pontos));
    }

    @Entao("o Overall do Atleta {int} e {string}")
    public void overallDoAtleta(Integer atletaId, String valor) {
        var carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        assertEquals(Double.parseDouble(valor), carta.getOverall(), 0.01);
    }

    @E("o saldo de pontos do Atleta {int} e {int}")
    public void saldoDePontos(Integer atletaId, Integer esperado) {
        var carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        assertEquals(esperado.doubleValue(), carta.getSaldoPontos(), 0.001);
    }

    @E("o historico de evolucao do Atleta {int} tem {int} registro")
    public void historicoEvolucao(Integer atletaId, Integer quantidade) {
        assertEquals(quantidade.longValue(), evolucaoRepositorio.contarPorAtleta(new AtletaId(atletaId)));
    }

    @Entao("o Overall simulado e {string}")
    public void overallSimulado(String valor) {
        assertNotNull(simulacao);
        assertEquals(Double.parseDouble(valor), simulacao.getOverallResultante(), 0.01);
    }

    @E("o Overall persistido do Atleta {int} continua {string}")
    public void overallPersistidoContinua(Integer atletaId, String valor) {
        var carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        assertEquals(Double.parseDouble(valor), carta.getOverall(), 0.01);
    }

    @E("o XP do Atleta {int} permanece em {string}")
    public void xpPermanece(Integer atletaId, String valor) {
        // delega ao mesmo repositorio usado nos demais steps; consulta via Reveal nao zera XP
        var carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        assertFalse(carta.isArquivada());
        assertEquals(Double.parseDouble(valor),
            revealServico.iniciar(new AtletaId(atletaId)).getXpLatente(), 0.01);
    }

    @Entao("o Reveal e rejeitado")
    public void revealRejeitado() {
        assertNotNull(excecao, "Esperava-se que o Reveal fosse rejeitado");
    }
}
