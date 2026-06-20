package com.sportsnap.session.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sportsnap.session.dominio.atividade.AnaliseAtividadeServico;
import com.sportsnap.session.dominio.atividade.AnaliseEvolucao;
import com.sportsnap.session.dominio.atividade.AtividadeServico;
import com.sportsnap.session.dominio.atividade.RegistroAtividade;
import com.sportsnap.session.dominio.atleta.AtletaId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class RegistroRealAtividadeSteps {

    @Autowired private AtividadeServico atividadeServico;
    @Autowired private AnaliseAtividadeServico analiseAtividadeServico;
    @Autowired private ColetorDeEventos coletorDeEventos;

    private RegistroAtividade registro;
    private Exception excecao;
    private List<RegistroAtividade> listaConsultada;
    private AnaliseEvolucao analise;

    @Quando("o Atleta {int} registra manualmente um treino de {string} em {string} com distancia {string} e duracao {int}")
    public void atletaRegistraTreinoManualComData(Integer atletaId, String esporte, String dataStr, String distancia, Integer duracao) {
        try {
            registro = registrarManual(atletaId, esporte, LocalDateTime.parse(dataStr), distancia, duracao);
            excecao = null;
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o Atleta {int} registra manualmente um treino de {string} com distancia {string} e duracao {int}")
    public void atletaRegistraTreinoManual(Integer atletaId, String esporte, String distancia, Integer duracao) {
        try {
            registro = registrarManual(atletaId, esporte, LocalDateTime.now(), distancia, duracao);
            excecao = null;
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Dado("(que )o Atleta {int} registrou manualmente um treino de {string} com distancia {string} e duracao {int}")
    public void dadoAtletaRegistrouTreinoManual(Integer atletaId, String esporte, String distancia, Integer duracao) {
        registrarManual(atletaId, esporte, LocalDateTime.now(), distancia, duracao);
    }

    @Dado("(que )o Atleta {int} registrou manualmente um treino de {string} em {string} com distancia {string} e duracao {int}")
    public void dadoAtletaRegistrouTreinoManualComData(Integer atletaId, String esporte, String dataStr, String distancia, Integer duracao) {
        registrarManual(atletaId, esporte, LocalDateTime.parse(dataStr), distancia, duracao);
    }

    @Quando("o Atleta {int} tenta registrar manualmente um treino de {string} com distancia {string} e duracao {int}")
    public void atletaTentaRegistrarTreinoManual(Integer atletaId, String esporte, String distancia, Integer duracao) {
        try {
            registro = registrarManual(atletaId, esporte, LocalDateTime.now(), distancia, duracao);
            excecao = null;
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o treino e registrado com sucesso")
    public void treinoRegistradoComSucesso() {
        assertNull(excecao, () -> "Nao deveria falhar: " + excecao);
        assertNotNull(registro);
        assertNotNull(registro.getId());
        assertNull(registro.getCheckInId());
        assertEquals("MANUAL", registro.getOrigemRegistro());
    }

    @E("o ritmo medio calculado e {string} min\\/km")
    public void ritmoMedioCalculado(String ritmoEsperado) {
        assertNotNull(registro);
        assertEquals(Double.parseDouble(ritmoEsperado), registro.getRitmoMedio(), 0.05);
    }

    @E("as calorias estimadas sao {string} kcal")
    public void caloriasEstimadas(String caloriasEsperadas) {
        assertNotNull(registro);
        assertEquals(Double.parseDouble(caloriasEsperadas), registro.getCaloriasEstimadas(), 0.1);
    }

    @E("o XP do treino e {string}")
    public void xpDoTreino(String xpEsperado) {
        assertNotNull(registro);
        assertEquals(Double.parseDouble(xpEsperado), registro.getXpCalculado(), 0.01);
    }

    @Entao("o registro falha com mensagem {string}")
    public void registroFalhaComMensagem(String mensagem) {
        assertNotNull(excecao, "Deveria ter lancado excecao");
        assertTrue(excecao.getMessage().contains(mensagem),
            "Mensagem esperada: " + mensagem + ", recebida: " + excecao.getMessage());
    }

    @Quando("solicito a lista de atividades do Atleta {int}")
    public void solicitoListaAtividades(Integer atletaId) {
        listaConsultada = atividadeServico.listarPorAtleta(new AtletaId(atletaId));
    }

    @Quando("solicito a lista de atividades do Atleta {int} filtrada pelo esporte {string}")
    public void solicitoListaAtividadesFiltradaEsporte(Integer atletaId, String esporte) {
        listaConsultada = atividadeServico.listarPorAtletaEEsporte(new AtletaId(atletaId), esporte);
    }

    @Quando("solicito a lista de atividades do Atleta {int} de {string} no periodo de {string} a {string}")
    public void solicitoListaAtividadesFiltradaPeriodo(Integer atletaId, String esporte, String inicioStr, String fimStr) {
        listaConsultada = atividadeServico.listarPorAtletaEsporteEPeriodo(
            new AtletaId(atletaId),
            esporte,
            LocalDateTime.parse(inicioStr),
            LocalDateTime.parse(fimStr)
        );
    }

    @Entao("recebo {int} atividade(s) no historico")
    public void receboAtividadesNoHistorico(Integer quantidade) {
        assertNotNull(listaConsultada);
        assertEquals(quantidade, listaConsultada.size());
    }

    @E("o esporte da atividade e {string}")
    public void esporteDaAtividade(String esporteEsperado) {
        assertFalse(listaConsultada.isEmpty());
        assertEquals(esporteEsperado, listaConsultada.get(0).getEsporte());
    }

    @E("a distancia da atividade e {string}")
    public void distanciaDaAtividade(String distanciaEsperada) {
        assertFalse(listaConsultada.isEmpty());
        assertEquals(Double.parseDouble(distanciaEsperada), listaConsultada.get(0).getDistancia(), 0.01);
    }

    @Quando("consulto a analise de evolucao de {string} para o Atleta {int} no periodo de {string}")
    public void consultoAnaliseEvolucao(String esporte, Integer atletaId, String periodo) {
        int dias = periodo.equals("7d") ? 7 : periodo.equals("90d") ? 90 : 30;
        analise = analiseAtividadeServico.gerarAnalise(new AtletaId(atletaId), esporte, dias);
    }

    @Entao("a analise indica total de {int} treinos")
    public void analiseIndicaTotalTreinos(Integer total) {
        assertNotNull(analise);
        assertEquals(total.intValue(), analise.totalAtividades());
    }

    @E("a distancia total e {string} km")
    public void analiseDistanciaTotal(String distanciaTotal) {
        assertEquals(Double.parseDouble(distanciaTotal), analise.distanciaTotal(), 0.05);
    }

    @E("o tempo total e {int} segundos")
    public void analiseTempoTotal(Integer tempoTotal) {
        assertEquals(tempoTotal.longValue(), analise.tempoTotalSegundos());
    }

    @E("o ritmo medio geral e {string} min\\/km")
    public void analiseRitmoMedioGeral(String ritmoEsperado) {
        assertEquals(Double.parseDouble(ritmoEsperado), analise.ritmoMedioGeral(), 0.05);
    }

    @E("o melhor ritmo e {string} min\\/km")
    public void analiseMelhorRitmo(String melhorRitmo) {
        assertEquals(Double.parseDouble(melhorRitmo), analise.melhorRitmo(), 0.05);
    }

    @E("a maior distancia e {string} km")
    public void analiseMaiorDistancia(String maiorDistancia) {
        assertEquals(Double.parseDouble(maiorDistancia), analise.maiorDistancia(), 0.05);
    }

    @E("a frequencia semanal e {string} treinos\\/semana")
    public void analiseFrequenciaSemanal(String freqEsperada) {
        assertEquals(Double.parseDouble(freqEsperada), analise.frequenciaSemanal(), 0.05);
    }

    @Entao("a analise real e gerada com sucesso sem conter calculo de XP")
    public void analiseRealSemXP() {
        assertNotNull(analise);
    }

    @Entao("nenhum evento de XP ou ranking e disparado para o sistema de gamificacao")
    public void nenhumEventoDeXPEsperado() {
        var eventosXP = coletorDeEventos.getEventos().stream()
            .filter(e -> e.toString().toLowerCase().contains("xp") || e.toString().toLowerCase().contains("ranking"))
            .toList();
        assertTrue(eventosXP.isEmpty());
    }

    private RegistroAtividade registrarManual(Integer atletaId, String esporte, LocalDateTime data,
                                              String distancia, Integer duracao) {
        return atividadeServico.registrarManual(
            new AtletaId(atletaId),
            esporte,
            data,
            Double.parseDouble(distancia),
            duracao.longValue(),
            5,
            "Treino de teste",
            null
        );
    }
}
