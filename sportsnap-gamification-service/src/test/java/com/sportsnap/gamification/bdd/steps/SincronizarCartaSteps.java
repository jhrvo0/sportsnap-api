package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.potencial.StatusPotencial;
import com.sportsnap.gamification.dominio.potencial.StatusPotencialRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.Licenca;
import com.sportsnap.gamification.dominio.sincronizacao.LicencaRepositorio;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico.CartaSincronizadaEvento;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SincronizarCartaSteps {

    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private StatusPotencialRepositorio statusRepositorio;
    @Autowired private LicencaRepositorio licencaRepositorio;
    @Autowired private SincronizacaoServico sincronizacaoServico;
    @Autowired private ColetorDeEventos coletorDeEventos;

    private Exception excecao;
    private boolean elegibilidade;
    private StatusPotencial shadowStats;
    private double overallReferencia;

    @Dado("que o Atleta {int} possui CartaOficial com atributos iniciais")
    public void atletaPossuiCartaInicial(Integer atletaId) {
        var aId = new AtletaId(atletaId);
        var atributos = new ArrayList<AtributoEsportivo>();
        atributos.add(new AtributoEsportivo("Resistencia", 50.0, 1.0, "corrida"));
        atributos.add(new AtributoEsportivo("Velocidade", 50.0, 1.0, "corrida"));
        cartaRepositorio.salvar(new CartaOficial(aId, atributos));
    }

    @E("o Atleta {int} acumulou {int} XP")
    public void atletaAcumulouXp(Integer atletaId, Integer xp) {
        var aId = new AtletaId(atletaId);
        var status = statusRepositorio.obterPorAtleta(aId)
            .orElseGet(() -> new StatusPotencial(aId));
        status.acumularXp((double) xp);
        statusRepositorio.salvar(status);
    }

    @E("o Atleta {int} possui Licenca adquirida apos a ultima sincronizacao")
    public void atletaPossuiLicenca(Integer atletaId) {
        licencaRepositorio.registrar(new Licenca(new AtletaId(atletaId), LocalDateTime.now()));
    }

    @Quando("o Atleta {int} sincroniza a Carta")
    public void atletaSincronizaCarta(Integer atletaId) {
        sincronizacaoServico.sincronizar(new AtletaId(atletaId));
    }

    @Quando("o Atleta {int} tenta sincronizar a Carta")
    public void atletaTentaSincronizar(Integer atletaId) {
        try {
            sincronizacaoServico.sincronizar(new AtletaId(atletaId));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o XP do Atleta {int} fica zerado")
    public void xpZerado(Integer atletaId) {
        var status = statusRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        assertEquals(0.0, status.getXpAcumulado(), 0.001);
    }

    @E("a ultima sincronizacao do Atleta {int} e registrada")
    public void ultimaSincronizacaoRegistrada(Integer atletaId) {
        var carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        assertNotNull(carta.getUltimaSincronizacao());
    }

    @E("o Overall da Carta e maior que zero")
    public void overallMaiorQueZero() {
        List<CartaOficial> todas = cartaRepositorio.listarTodas();
        assertFalse(todas.isEmpty());
        assertTrue(todas.stream().anyMatch(c -> c.getOverall() > 0));
    }

    @E("um evento CartaSincronizadaEvento e publicado")
    public void eventoCartaSincronizada() {
        assertTrue(coletorDeEventos.getEventos().stream()
            .anyMatch(e -> e instanceof CartaSincronizadaEvento));
    }

    @Entao("a sincronizacao e rejeitada")
    public void sincronizacaoRejeitada() {
        assertNotNull(excecao);
    }

    @E("a Carta do Atleta {int} nao esta sincronizada")
    public void cartaNaoSincronizada(Integer atletaId) {
        var carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        assertFalse(carta.isSincronizada());
    }

    @Quando("consulto a elegibilidade do Atleta {int}")
    public void consultoElegibilidade(Integer atletaId) {
        elegibilidade = sincronizacaoServico.isElegivel(new AtletaId(atletaId));
    }

    @Entao("a elegibilidade e positiva")
    public void elegibilidadePositiva() {
        assertTrue(elegibilidade);
    }

    @Entao("a elegibilidade e negativa")
    public void elegibilidadeNegativa() {
        assertFalse(elegibilidade);
    }

    @Quando("consulto os shadow stats do Atleta {int}")
    public void consultoShadowStats(Integer atletaId) {
        shadowStats = sincronizacaoServico.consultarShadowStats(new AtletaId(atletaId));
    }

    @Entao("o XP acumulado e {string}")
    public void xpAcumuladoE(String valor) {
        assertEquals(Double.parseDouble(valor), shadowStats.getXpAcumulado(), 0.001);
    }

    @E("o overall da Carta do Atleta {int} e capturado como referencia")
    public void capturarOverallReferencia(Integer atletaId) {
        overallReferencia = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId))
            .orElseThrow().getOverall();
    }

    @Entao("o Overall do Atleta {int} e maior que a referencia")
    public void overallMaiorQueReferencia(Integer atletaId) {
        var carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        assertTrue(carta.getOverall() > overallReferencia,
            "Esperado overall > " + overallReferencia + ", obtido " + carta.getOverall());
    }
}
