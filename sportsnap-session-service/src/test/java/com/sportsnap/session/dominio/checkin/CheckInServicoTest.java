package com.sportsnap.session.dominio.checkin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.evento.EventoBarramento;
import com.sportsnap.session.dominio.evento.EventoObservador;
import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.dominio.spot.SpotId;
import com.sportsnap.session.infraestrutura.memoria.CheckInRepositorioMemoria;
import com.sportsnap.session.infraestrutura.memoria.SessaoRepositorioMemoria;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckInServicoTest {

    private final CheckInRepositorioMemoria checkIns = new CheckInRepositorioMemoria();
    private final SessaoRepositorioMemoria sessoes = new SessaoRepositorioMemoria();
    private final List<Object> eventos = new ArrayList<>();
    private final EventoBarramento barramento = new EventoBarramento() {
        @Override
        public <E> void adicionar(EventoObservador<E> observador, Class<E> tipoEvento) {
        }

        @Override
        public void postar(Object evento) {
            eventos.add(evento);
        }
    };
    private CheckInServico servico;

    @BeforeEach
    void setUp() {
        servico = new CheckInServico(checkIns, sessoes, barramento);
    }

    @Test
    void checkoutPersisteHorarioEReapareceNaListagemDoAtleta() {
        var sessao = sessoes.salvar(new com.sportsnap.session.dominio.sessao.Sessao(
            new SpotId(1),
            new Periodo(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1)),
            "Treino aberto"
        ));
        var checkIn = servico.realizar(new AtletaId(1), sessao.getId(), new Coordenada(-8.0, -34.0));

        var finalizado = servico.checkout(checkIn.getId());

        assertNotNull(finalizado.getCheckoutHorario());
        assertEquals(finalizado.getCheckoutHorario(), servico.listarPorAtleta(new AtletaId(1)).get(0).getCheckoutHorario());
    }

    @Test
    void checkoutDuplicadoERejeitado() {
        var sessao = sessoes.salvar(new com.sportsnap.session.dominio.sessao.Sessao(
            new SpotId(1),
            new Periodo(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1)),
            "Treino aberto"
        ));
        var checkIn = servico.realizar(new AtletaId(1), sessao.getId(), new Coordenada(-8.0, -34.0));
        servico.checkout(checkIn.getId());

        var erro = assertThrows(IllegalStateException.class, () -> servico.checkout(checkIn.getId()));

        assertTrue(erro.getMessage().contains("checkout"));
    }

    @Test
    void checkoutTardioEmSessaoEncerradaERejeitado() {
        var sessao = sessoes.salvar(new com.sportsnap.session.dominio.sessao.Sessao(
            new SessaoId(1),
            new SpotId(1),
            new Periodo(LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1)),
            "Treino encerrado",
            false
        ));
        var checkIn = checkIns.salvar(new CheckIn(
            new AtletaId(1),
            sessao.getId(),
            LocalDateTime.now().minusHours(2),
            new Coordenada(-8.0, -34.0)
        ));

        var erro = assertThrows(IllegalStateException.class, () -> servico.checkout(checkIn.getId()));

        assertTrue(erro.getMessage().contains("encerrada"));
    }
}
