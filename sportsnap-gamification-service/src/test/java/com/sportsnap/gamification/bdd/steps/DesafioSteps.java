package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.desafio.Cadencia;
import com.sportsnap.gamification.dominio.desafio.CriterioDesafio;
import com.sportsnap.gamification.dominio.desafio.Desafio;
import com.sportsnap.gamification.dominio.desafio.DesafioServico;
import com.sportsnap.gamification.dominio.desafio.InsigniaRepositorio;
import com.sportsnap.gamification.dominio.desafio.MotorDesafios;
import com.sportsnap.gamification.dominio.desafio.ProgressoDesafio;
import com.sportsnap.gamification.dominio.desafio.ProgressoDesafioRepositorio;
import com.sportsnap.gamification.dominio.desafio.TipoCriterio;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico.CartaSincronizadaEvento;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DesafioSteps {

    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private DesafioServico desafioServico;
    @Autowired private MotorDesafios motorDesafios;
    @Autowired private ProgressoDesafioRepositorio progressoRepositorio;
    @Autowired private InsigniaRepositorio insigniaRepositorio;

    private Integer ultimoDesafioId;
    private ProgressoDesafio ultimoProgresso;
    private Exception excecao;

    @Dado("que existe um desafio permanente {string} com meta de {int} sincronizacoes e insignia {string}")
    public void desafioPermanenteSincronizacoes(String titulo, Integer meta, String insignia) {
        Desafio desafio = new Desafio(titulo,
            List.of(CriterioDesafio.de(TipoCriterio.CONTAGEM_SINCRONIZACOES, meta)),
            null, null, true, insignia, List.of(), Cadencia.NENHUMA, false);
        ultimoDesafioId = desafioServico.definir(desafio).getId();
    }

    @Dado("que existe um desafio expirado {string} com insignia {string}")
    public void desafioExpirado(String titulo, String insignia) {
        Desafio desafio = new Desafio(titulo,
            List.of(CriterioDesafio.de(TipoCriterio.CONTAGEM_SINCRONIZACOES, 1)),
            LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5),
            false, insignia, List.of(), Cadencia.NENHUMA, false);
        ultimoDesafioId = desafioServico.definir(desafio).getId();
    }

    @E("que existe um desafio {string} encadeado apos o ultimo definido com insignia {string}")
    public void desafioEncadeado(String titulo, String insignia) {
        Desafio desafio = new Desafio(titulo,
            List.of(CriterioDesafio.de(TipoCriterio.CONTAGEM_SINCRONIZACOES, 1)),
            null, null, true, insignia, List.of(ultimoDesafioId), Cadencia.NENHUMA, false);
        ultimoDesafioId = desafioServico.definir(desafio).getId();
    }

    @Quando("o Atleta {int} aceita o ultimo desafio")
    public void aceitaUltimoDesafio(Integer atletaId) {
        excecao = null;
        try {
            ultimoProgresso = desafioServico.aceitar(new AtletaId(atletaId), ultimoDesafioId, LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Atleta {int} registra {int} sincronizacoes")
    public void registraSincronizacoes(Integer atletaId, Integer quantidade) {
        CartaOficial carta = cartaRepositorio.obterPorAtleta(new AtletaId(atletaId)).orElseThrow();
        for (int i = 0; i < quantidade; i++) {
            motorDesafios.aoSincronizar(new CartaSincronizadaEvento(carta, 10.0));
        }
    }

    @Quando("o Atleta {int} cancela o ultimo progresso")
    public void cancelaUltimoProgresso(Integer atletaId) {
        ultimoProgresso = desafioServico.cancelar(ultimoProgresso.getId());
    }

    @Entao("o ultimo progresso do Atleta {int} esta {string}")
    public void ultimoProgressoEsta(Integer atletaId, String status) {
        ProgressoDesafio atual = progressoRepositorio.obterPorId(ultimoProgresso.getId()).orElseThrow();
        assertEquals(status, atual.getStatus().name());
    }

    @Entao("o Atleta {int} possui {int} insignia")
    public void atletaPossuiInsignias(Integer atletaId, Integer quantidade) {
        List<?> insignias = insigniaRepositorio.listarPorAtleta(new AtletaId(atletaId));
        assertEquals(quantidade.intValue(), insignias.size());
    }

    @Entao("a aceitacao e rejeitada")
    public void aceitacaoRejeitada() {
        assertNotNull(excecao, "Esperava-se que a aceitacao fosse rejeitada");
    }
}
