package com.sportsnap.session.dominio.atividade;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInId;
import com.sportsnap.session.dominio.checkin.CheckInRepositorio;
import com.sportsnap.session.dominio.evento.EventoBarramento;

import java.util.List;

public class AtividadeServico {

    private final RegistroAtividadeRepositorio repositorio;
    private final CheckInRepositorio checkInRepositorio;
    private final EventoBarramento barramento;

    public AtividadeServico(RegistroAtividadeRepositorio repositorio,
                             CheckInRepositorio checkInRepositorio,
                             EventoBarramento barramento) {
        notNull(repositorio, "O repositorio de RegistroAtividade nao pode ser nulo");
        notNull(checkInRepositorio, "O repositorio de CheckIn nao pode ser nulo");
        notNull(barramento, "O barramento de eventos nao pode ser nulo");
        this.repositorio = repositorio;
        this.checkInRepositorio = checkInRepositorio;
        this.barramento = barramento;
    }

    public RegistroAtividade registrar(CheckInId checkInId, double distancia, long duracaoSegundos,
                                        Intensidade intensidade) {
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");

        CheckIn checkIn = checkInRepositorio.obter(checkInId)
            .orElseThrow(() -> new IllegalArgumentException("CheckIn nao encontrado: " + checkInId));

        if (checkIn.isCancelado()) {
            throw new IllegalStateException("Nao e possivel registrar atividade em CheckIn cancelado");
        }

        var registro = new RegistroAtividade(checkInId, distancia, duracaoSegundos, intensidade);
        var salvo = repositorio.salvar(registro);

        checkIn.marcarAtividadeRegistrada();
        checkInRepositorio.salvar(checkIn);

        barramento.postar(new AtividadeRegistradaEvento(salvo, checkIn.getAtletaId()));
        return salvo;
    }

    public List<RegistroAtividade> listarPorCheckIn(CheckInId checkInId) {
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");
        return repositorio.listarPorCheckIn(checkInId);
    }

    public double calcularXpTotalDoCheckIn(CheckInId checkInId) {
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");
        return repositorio.listarPorCheckIn(checkInId).stream()
            .mapToDouble(RegistroAtividade::getXpCalculado)
            .sum();
    }

    public static class AtividadeRegistradaEvento {
        private final RegistroAtividade registro;
        private final AtletaId atletaId;

        AtividadeRegistradaEvento(RegistroAtividade registro, AtletaId atletaId) {
            this.registro = registro;
            this.atletaId = atletaId;
        }

        public RegistroAtividade getRegistro() {
            return registro;
        }

        public AtletaId getAtletaId() {
            return atletaId;
        }
    }
}
