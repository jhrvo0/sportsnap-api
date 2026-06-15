package com.sportsnap.gamification.bdd.steps;

import com.sportsnap.gamification.dominio.analise.AnaliseServico;
import com.sportsnap.gamification.dominio.analise.PercentilAtributo;
import com.sportsnap.gamification.dominio.analise.Projecao;
import com.sportsnap.gamification.dominio.analise.RecomendacaoSimilaridade;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.carta.AtributoEsportivo;
import com.sportsnap.gamification.dominio.carta.CartaOficial;
import com.sportsnap.gamification.dominio.carta.CartaOficialRepositorio;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucao;
import com.sportsnap.gamification.dominio.evolucao.RegistroEvolucaoRepositorio;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AnaliseSteps {

    @Autowired private CartaOficialRepositorio cartaRepositorio;
    @Autowired private RegistroEvolucaoRepositorio evolucaoRepositorio;
    @Autowired private AnaliseServico analiseServico;

    private PercentilAtributo percentil;
    private List<RecomendacaoSimilaridade> similares;
    private Projecao projecao;
    private Exception excecao;

    @Dado("que existe base elegivel no atributo {string} com valores {string}")
    public void baseElegivel(String atributo, String valores) {
        String[] partes = valores.split(",");
        for (int i = 0; i < partes.length; i++) {
            double valor = Double.parseDouble(partes[i].trim());
            var carta = new CartaOficial(new AtletaId(i + 1),
                List.of(new AtributoEsportivo(atributo, valor, 1.0, "corrida")),
                valor, LocalDateTime.now());
            cartaRepositorio.salvar(carta);
        }
    }

    @Dado("que o Atleta {int} tem Carta nao sincronizada no atributo {string} valor {int}")
    public void cartaNaoSincronizada(Integer atletaId, String atributo, Integer valor) {
        var carta = new CartaOficial(new AtletaId(atletaId),
            List.of(new AtributoEsportivo(atributo, valor.doubleValue(), 1.0, "corrida")),
            valor.doubleValue(), null);
        cartaRepositorio.salvar(carta);
    }

    @Dado("que o Atleta {int} tem Carta sincronizada com Overall {int} e {int} registros de evolucao crescente")
    public void cartaComHistorico(Integer atletaId, Integer overall, Integer quantidade) {
        var carta = new CartaOficial(new AtletaId(atletaId),
            List.of(new AtributoEsportivo("Geral", overall.doubleValue(), 1.0, "corrida")),
            overall.doubleValue(), LocalDateTime.now());
        cartaRepositorio.salvar(carta);
        for (int k = 0; k < quantidade; k++) {
            double anterior = overall - (quantidade - k) * 5.0;
            evolucaoRepositorio.inserir(new RegistroEvolucao(new AtletaId(atletaId),
                anterior, anterior + 5.0, LocalDateTime.now().minusDays(quantidade - k)));
        }
    }

    @Quando("consulto o percentil do Atleta {int} no atributo {string}")
    public void consultoPercentil(Integer atletaId, String atributo) {
        try {
            percentil = analiseServico.percentil(new AtletaId(atletaId), atributo);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o percentil e {string}")
    public void percentilEsperado(String valor) {
        assertNotNull(percentil);
        assertEquals(Double.parseDouble(valor), percentil.getPercentil(), 0.01);
    }

    @Quando("consulto os atletas similares ao Atleta {int} na modalidade {string}")
    public void consultoSimilares(Integer atletaId, String modalidade) {
        similares = analiseServico.similares(new AtletaId(atletaId), 5, modalidade);
    }

    @Entao("o atleta mais similar ao Atleta {int} e o Atleta {int}")
    public void atletaMaisSimilar(Integer atletaId, Integer esperado) {
        assertFalse(similares.isEmpty());
        assertEquals(esperado.intValue(), similares.get(0).getAtletaId().getId());
    }

    @Quando("consulto a projecao do Atleta {int}")
    public void consultoProjecao(Integer atletaId) {
        try {
            projecao = analiseServico.projecao(new AtletaId(atletaId));
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("a projecao do Atleta {int} indica crescimento")
    public void projecaoCrescimento(Integer atletaId) {
        assertNotNull(projecao);
        assertTrue(projecao.getOverallProjetado() > projecao.getOverallAtual(),
            "Esperado projetado > atual");
    }

    @Entao("a analise e indisponivel")
    public void analiseIndisponivel() {
        assertNotNull(excecao, "Esperava-se que a analise fosse indisponivel");
    }
}
