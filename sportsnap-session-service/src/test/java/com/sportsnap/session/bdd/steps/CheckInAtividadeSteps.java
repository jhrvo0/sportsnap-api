package com.sportsnap.session.bdd.steps;

import com.sportsnap.session.dominio.atividade.AtividadeServico;
import com.sportsnap.session.dominio.atividade.Intensidade;
import com.sportsnap.session.dominio.atividade.RegistroAtividade;
import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckIn.CheckInCanceladoEvento;
import com.sportsnap.session.dominio.checkin.CheckIn.CheckInRealizadoEvento;
import com.sportsnap.session.dominio.checkin.CheckInServico;
import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.Sessao;
import com.sportsnap.session.dominio.sessao.SessaoServico;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.Spot;
import com.sportsnap.session.dominio.spot.SpotServico;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckInAtividadeSteps {

    @Autowired private SpotServico spotServico;
    @Autowired private SessaoServico sessaoServico;
    @Autowired private CheckInServico checkInServico;
    @Autowired private AtividadeServico atividadeServico;
    @Autowired private ColetorDeEventos coletorDeEventos;

    private Spot spot;
    private Sessao sessao;
    private CheckIn checkIn;
    private RegistroAtividade registro;
    private Exception excecao;
    private List<CheckIn> checkInsConsultados;
    private List<AtletaId> atletasMatchados;

    @Dado("que existe uma Sessao ativa")
    public void sessaoAtiva() {
        spot = spotServico.cadastrar("Spot Ativa", new Coordenada(-8.0631, -34.8711), "Teste");
        LocalDateTime agora = LocalDateTime.now();
        sessao = sessaoServico.cadastrar(spot.getId(),
            new Periodo(agora.minusHours(1), agora.plusHours(2)), "Sessao ativa");
    }

    @Dado("que existe uma Sessao ja encerrada")
    public void sessaoEncerrada() {
        spot = spotServico.cadastrar("Spot Ontem", new Coordenada(-8.06, -34.87), "Teste");
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);
        sessao = sessaoServico.cadastrar(spot.getId(),
            new Periodo(ontem.minusHours(2), ontem.minusHours(1)), "Sessao passada");
    }

    @Quando("o Atleta {int} realiza CheckIn na Sessao")
    public void atletaRealizaCheckIn(Integer atletaId) {
        checkIn = checkInServico.realizar(new AtletaId(atletaId), sessao.getId(),
            spot.getCoordenada());
    }

    @Quando("o Atleta {int} tenta realizar CheckIn")
    public void atletaTentaCheckIn(Integer atletaId) {
        try {
            checkIn = checkInServico.realizar(new AtletaId(atletaId), sessao.getId(),
                spot.getCoordenada());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @E("o Atleta {int} ja realizou CheckIn nesta Sessao")
    public void atletaJaRealizouCheckIn(Integer atletaId) {
        checkIn = checkInServico.realizar(new AtletaId(atletaId), sessao.getId(),
            spot.getCoordenada());
    }

    @Quando("o Atleta {int} tenta realizar CheckIn novamente")
    public void atletaTentaCheckInNovamente(Integer atletaId) {
        try {
            checkInServico.realizar(new AtletaId(atletaId), sessao.getId(),
                spot.getCoordenada());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o CheckIn e registrado com sucesso")
    public void checkInRegistrado() {
        assertNull(excecao, () -> "Nao deveria falhar: " + excecao);
        assertNotNull(checkIn);
        assertNotNull(checkIn.getId());
    }

    @Entao("o CheckIn e rejeitado com mensagem {string}")
    public void checkInRejeitadoComMensagem(String mensagem) {
        assertNotNull(excecao);
        assertTrue(excecao.getMessage().contains(mensagem),
            "Mensagem esperada: " + mensagem + ", recebida: " + excecao.getMessage());
    }

    @Entao("o CheckIn e rejeitado")
    public void checkInRejeitado() {
        assertNotNull(excecao, "Deveria ter lancado excecao");
    }

    @E("um evento CheckInRealizadoEvento e publicado")
    public void eventoCheckInRealizado() {
        assertTrue(coletorDeEventos.getEventos().stream()
            .anyMatch(e -> e instanceof CheckInRealizadoEvento));
    }

    @E("o Atleta {int} realizou CheckIn na Sessao")
    public void atletaRealizouCheckIn(Integer atletaId) {
        checkIn = checkInServico.realizar(new AtletaId(atletaId), sessao.getId(),
            spot.getCoordenada());
    }

    @Quando("o Atleta registra atividade com distancia {string}, duracao {int} e intensidade {string}")
    public void atletaRegistraAtividade(String distancia, Integer duracao, String intensidade) {
        registro = atividadeServico.registrar(checkIn.getId(), Double.parseDouble(distancia),
            duracao.longValue(), Intensidade.apartirDeTexto(intensidade));
    }

    @E("o Atleta registrou atividade com distancia {string}, duracao {int} e intensidade {string}")
    public void atletaRegistrouAtividade(String distancia, Integer duracao, String intensidade) {
        registro = atividadeServico.registrar(checkIn.getId(), Double.parseDouble(distancia),
            duracao.longValue(), Intensidade.apartirDeTexto(intensidade));
    }

    @Entao("o XP calculado e {string}")
    public void xpCalculado(String xpEsperado) {
        assertNotNull(registro);
        assertEquals(Double.parseDouble(xpEsperado), registro.getXpCalculado(), 0.001);
    }

    @E("um evento AtividadeRegistradaEvento e publicado")
    public void eventoAtividadeRegistrada() {
        assertTrue(coletorDeEventos.getEventos().stream()
            .anyMatch(e -> e instanceof AtividadeServico.AtividadeRegistradaEvento));
    }

    @Quando("cancelo o CheckIn")
    public void canceloCheckIn() {
        try {
            checkInServico.cancelar(checkIn.getId());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("tento cancelar o CheckIn")
    public void tentoCancelarCheckIn() {
        try {
            checkInServico.cancelar(checkIn.getId());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Entao("o CheckIn fica marcado como cancelado")
    public void checkInCancelado() {
        assertNull(excecao, () -> "Cancelamento nao deveria falhar: " + excecao);
        var recuperado = checkInServico.obter(checkIn.getId());
        assertTrue(recuperado.isCancelado());
    }

    @E("um evento CheckInCanceladoEvento e publicado")
    public void eventoCheckInCancelado() {
        assertTrue(coletorDeEventos.getEventos().stream()
            .anyMatch(e -> e instanceof CheckInCanceladoEvento));
    }

    @Entao("o cancelamento e rejeitado")
    public void cancelamentoRejeitado() {
        assertNotNull(excecao, "Deveria ter lancado excecao");
    }

    @Quando("consulto os CheckIns do Atleta {int}")
    public void consultoCheckIns(Integer atletaId) {
        checkInsConsultados = checkInServico.listarPorAtleta(new AtletaId(atletaId));
    }

    @Entao("recebo {int} CheckIn")
    public void receboQuantidadeCheckIns(Integer quantidade) {
        assertEquals(quantidade, checkInsConsultados.size());
    }

    @Quando("solicito a lista de atletas matchados na Sessao")
    public void solicitoMatch() {
        atletasMatchados = checkInServico.listarAtletasComCheckIn(sessao.getId());
    }

    @Entao("recebo {int} atletas matchados")
    public void receboAtletasMatchados(Integer quantidade) {
        assertEquals(quantidade, atletasMatchados.size());
    }
}
