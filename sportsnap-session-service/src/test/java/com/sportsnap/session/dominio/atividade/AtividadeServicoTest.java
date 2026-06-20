package com.sportsnap.session.dominio.atividade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInId;
import com.sportsnap.session.dominio.evento.EventoBarramento;
import com.sportsnap.session.dominio.evento.EventoObservador;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.spot.Coordenada;
import com.sportsnap.session.infraestrutura.memoria.CheckInRepositorioMemoria;
import com.sportsnap.session.infraestrutura.memoria.RegistroAtividadeRepositorioMemoria;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AtividadeServicoTest {

    private final RegistroAtividadeRepositorioMemoria atividades = new RegistroAtividadeRepositorioMemoria();
    private final CheckInRepositorioMemoria checkIns = new CheckInRepositorioMemoria();
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
    private final AtividadeServico servico = new AtividadeServico(atividades, checkIns, barramento);

    @Test
    void registrarManualSalvaSemCheckInSemXpESemEventoDeGamificacao() {
        var registro = servico.registrarManual(
            new AtletaId(1),
            "corrida",
            LocalDateTime.now(),
            5.0,
            1800,
            7,
            "Treino livre",
            "{\"terreno\":\"rua\"}"
        );

        assertNull(registro.getCheckInId());
        assertEquals("MANUAL", registro.getOrigemRegistro());
        assertEquals(0.0, registro.getXpCalculado());
        assertEquals(1, atividades.buscarPorAtleta(new AtletaId(1)).size());
        assertTrue(eventos.isEmpty());
    }

    @Test
    void registrarComCheckInRejeitaCheckInComCheckoutRealizado() {
        var checkIn = checkIns.salvar(new CheckIn(
            new CheckInId(1),
            new AtletaId(1),
            new SessaoId(1),
            LocalDateTime.now().minusMinutes(10),
            new Coordenada(-8.0, -34.0),
            false,
            false,
            LocalDateTime.now()
        ));

        var erro = assertThrows(IllegalStateException.class, () -> servico.registrarComCheckIn(
            checkIn.getAtletaId(),
            checkIn.getId(),
            "CORRIDA",
            LocalDateTime.now(),
            5.0,
            1800,
            Intensidade.ALTA,
            15.0,
            null,
            null,
            "CHECKIN",
            null
        ));

        assertTrue(erro.getMessage().contains("checkout"));
    }
}
