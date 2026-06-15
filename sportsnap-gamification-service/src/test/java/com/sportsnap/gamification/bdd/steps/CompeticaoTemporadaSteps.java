package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.competicao.CompeticaoServico;
import com.sportsnap.gamification.dominio.competicao.Confronto;
import com.sportsnap.gamification.dominio.competicao.PontuacaoRanking;
import com.sportsnap.gamification.dominio.competicao.PosicaoLiga;
import com.sportsnap.gamification.dominio.competicao.Temporada;
import com.sportsnap.gamification.dominio.competicao.TemporadaRepositorio;
import com.sportsnap.gamification.dominio.competicao.TemporadaServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CompeticaoTemporadaSteps {

    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private CompeticaoServico competicaoServico;
    @Autowired private TemporadaServico temporadaServico;
    @Autowired private TemporadaRepositorio temporadaRepositorio;

    private Integer ultimaTemporadaId;
    private Confronto ultimoConfronto;
    private List<PontuacaoRanking> classificacao;
    private Optional<PosicaoLiga> posicao;
    private Exception excecao;

    @Dado("que existe Temporada vigente para a modalidade {string}")
    public void temporadaVigente(String modalidade) {
        Temporada t = temporadaServico.criar(modalidade,
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        ultimaTemporadaId = t.getId();
    }

    @Dado("que crio uma Temporada futura para {string}")
    public void temporadaFutura(String modalidade) {
        Temporada t = temporadaServico.criar(modalidade,
            LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(10));
        ultimaTemporadaId = t.getId();
    }

    @Dado("que o Atleta {int} tem Carta sincronizada com Overall {int} na modalidade {string}")
    public void atletaCartaSincronizadaNaModalidade(Integer atletaId, Integer overall, String modalidade) {
        var carta = new CartaOficial(new AtletaId(atletaId),
            List.of(new AtributoEsportivo("Geral", overall.doubleValue(), 1.0, modalidade)),
            overall.doubleValue(), LocalDateTime.now());
        cartaRepositorio.salvar(carta);
        competicaoServico.registrarElegivel(new AtletaId(atletaId));
    }

    @Quando("o Atleta {int} enfrenta o Atleta {int} na modalidade {string}")
    public void atletaEnfrenta(Integer a, Integer b, String modalidade) {
        try {
            ultimoConfronto = competicaoServico.resolverConfronto(
                new AtletaId(a), new AtletaId(b), modalidade, LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o vencedor do confronto e o Atleta {int}")
    public void vencedorDoConfronto(Integer atletaId) {
        assertNotNull(ultimoConfronto);
        assertEquals(atletaId.intValue(), ultimoConfronto.getVencedorId().getId());
    }

    @E("o PR do Atleta {int} e maior que {int}")
    public void prMaiorQue(Integer atletaId, Integer limite) {
        double pr = prDoAtleta(atletaId);
        assertTrue(pr > limite, "Esperado PR > " + limite + ", obtido " + pr);
    }

    @E("o PR do Atleta {int} e menor que {int}")
    public void prMenorQue(Integer atletaId, Integer limite) {
        double pr = prDoAtleta(atletaId);
        assertTrue(pr < limite, "Esperado PR < " + limite + ", obtido " + pr);
    }

    @Entao("o confronto e rejeitado")
    public void confrontoRejeitado() {
        assertNotNull(excecao, "Esperava-se que o confronto fosse rejeitado");
    }

    @Quando("consulto a classificacao competitiva")
    public void consultoClassificacao() {
        classificacao = competicaoServico.classificacao();
    }

    @Entao("o primeiro colocado e o Atleta {int}")
    public void primeiroColocado(Integer atletaId) {
        assertFalse(classificacao.isEmpty());
        assertEquals(atletaId.intValue(), classificacao.get(0).getAtletaId().getId());
    }

    @Quando("consulto a posicao competitiva do Atleta {int}")
    public void consultoPosicaoCompetitiva(Integer atletaId) {
        posicao = competicaoServico.consultarPosicao(new AtletaId(atletaId));
    }

    @Entao("a posicao competitiva e {int}")
    public void posicaoCompetitiva(Integer esperada) {
        assertTrue(posicao.isPresent());
        assertEquals(esperada.intValue(), posicao.get().getPosicao());
    }

    @Entao("o atleta nao esta classificado")
    public void atletaNaoClassificado() {
        assertTrue(posicao.isEmpty());
    }

    @Quando("crio uma Temporada para {string} de {int} a {int} dias no futuro")
    public void crioTemporada(String modalidade, Integer diasInicio, Integer diasFim) {
        try {
            Temporada t = temporadaServico.criar(modalidade,
                LocalDateTime.now().plusDays(diasInicio), LocalDateTime.now().plusDays(diasFim));
            ultimaTemporadaId = t.getId();
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("a Temporada e criada")
    public void temporadaCriada() {
        assertNull(excecao);
        assertNotNull(ultimaTemporadaId);
    }

    @Entao("a criacao da Temporada e rejeitada")
    public void criacaoRejeitada() {
        assertNotNull(excecao, "Esperava-se que a criacao fosse rejeitada");
    }

    @Quando("cancelo essa Temporada")
    public void canceloTemporada() {
        try {
            temporadaServico.cancelar(ultimaTemporadaId, LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("a Temporada esta {string}")
    public void temporadaEsta(String status) {
        Temporada t = temporadaRepositorio.obterPorId(ultimaTemporadaId).orElseThrow();
        assertEquals(status, t.getStatus().name());
    }

    @Entao("o cancelamento e rejeitado")
    public void cancelamentoRejeitado() {
        assertNotNull(excecao, "Esperava-se que o cancelamento fosse rejeitado");
    }

    @Quando("encerro essa Temporada")
    public void encerroTemporada() {
        temporadaServico.encerrar(ultimaTemporadaId, LocalDateTime.now());
    }

    @Entao("o snapshot final tem {int} entradas")
    public void snapshotFinalTem(Integer quantidade) {
        Temporada t = temporadaRepositorio.obterPorId(ultimaTemporadaId).orElseThrow();
        assertEquals(quantidade.intValue(), t.getSnapshotFinal().size());
    }

    private double prDoAtleta(Integer atletaId) {
        return competicaoServico.classificacao().stream()
            .filter(p -> p.getAtletaId().getId() == atletaId)
            .map(PontuacaoRanking::getPr)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Atleta sem PR: " + atletaId));
    }
}
