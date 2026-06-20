package com.sportsnap.session.dominio.checkin;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.spot.Coordenada;

public class CheckIn {

    private final CheckInId id;
    private final AtletaId atletaId;
    private final SessaoId sessaoId;
    private final LocalDateTime horario;
    private final Coordenada coordenada;
    private boolean cancelado;
    private boolean atividadeRegistrada;
    private LocalDateTime checkoutHorario;

    public CheckIn(AtletaId atletaId, SessaoId sessaoId, LocalDateTime horario, Coordenada coordenada) {
        id = null;
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(sessaoId, "O id da Sessao nao pode ser nulo");
        notNull(horario, "O horario do CheckIn nao pode ser nulo");
        notNull(coordenada, "A coordenada do CheckIn nao pode ser nula");
        this.atletaId = atletaId;
        this.sessaoId = sessaoId;
        this.horario = horario;
        this.coordenada = coordenada;
        this.cancelado = false;
        this.atividadeRegistrada = false;
        this.checkoutHorario = null;
    }

    public CheckIn(CheckInId id, AtletaId atletaId, SessaoId sessaoId, LocalDateTime horario,
                   Coordenada coordenada, boolean cancelado, boolean atividadeRegistrada) {
        this(id, atletaId, sessaoId, horario, coordenada, cancelado, atividadeRegistrada, null);
    }

    public CheckIn(CheckInId id, AtletaId atletaId, SessaoId sessaoId, LocalDateTime horario,
                   Coordenada coordenada, boolean cancelado, boolean atividadeRegistrada,
                   LocalDateTime checkoutHorario) {
        notNull(id, "O id do CheckIn nao pode ser nulo");
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(sessaoId, "O id da Sessao nao pode ser nulo");
        notNull(horario, "O horario do CheckIn nao pode ser nulo");
        notNull(coordenada, "A coordenada do CheckIn nao pode ser nula");
        this.id = id;
        this.atletaId = atletaId;
        this.sessaoId = sessaoId;
        this.horario = horario;
        this.coordenada = coordenada;
        this.cancelado = cancelado;
        this.atividadeRegistrada = atividadeRegistrada;
        this.checkoutHorario = checkoutHorario;
    }

    public CheckInId getId() {
        return id;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public SessaoId getSessaoId() {
        return sessaoId;
    }

    public LocalDateTime getHorario() {
        return horario;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public boolean temAtividadeRegistrada() {
        return atividadeRegistrada;
    }

    public LocalDateTime getCheckoutHorario() {
        return checkoutHorario;
    }

    public boolean temCheckout() {
        return checkoutHorario != null;
    }

    public CheckInCanceladoEvento cancelar() {
        if (cancelado) {
            throw new IllegalStateException("O CheckIn ja esta cancelado");
        }
        if (atividadeRegistrada) {
            throw new IllegalStateException("Nao e possivel cancelar um CheckIn com atividade registrada");
        }
        this.cancelado = true;
        return new CheckInCanceladoEvento(this);
    }

    public void marcarAtividadeRegistrada() {
        if (cancelado) {
            throw new IllegalStateException("Nao e possivel registrar atividade em CheckIn cancelado");
        }
        if (checkoutHorario != null) {
            throw new IllegalStateException("Nao e possivel registrar atividade apos checkout");
        }
        this.atividadeRegistrada = true;
    }

    public void realizarCheckout(LocalDateTime horarioCheckout) {
        notNull(horarioCheckout, "O horario do checkout nao pode ser nulo");
        if (cancelado) {
            throw new IllegalStateException("Nao e possivel fazer checkout de CheckIn cancelado");
        }
        if (checkoutHorario != null) {
            throw new IllegalStateException("CheckIn ja possui checkout");
        }
        this.checkoutHorario = horarioCheckout;
    }

    public static class CheckInEvento {
        private final CheckIn checkIn;

        private CheckInEvento(CheckIn checkIn) {
            this.checkIn = checkIn;
        }

        public CheckIn getCheckIn() {
            return checkIn;
        }
    }

    public static class CheckInRealizadoEvento extends CheckInEvento {
        CheckInRealizadoEvento(CheckIn checkIn) {
            super(checkIn);
        }
    }

    public static class CheckInCanceladoEvento extends CheckInEvento {
        CheckInCanceladoEvento(CheckIn checkIn) {
            super(checkIn);
        }
    }
}
