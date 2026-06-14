package com.sportsnap.session.dominio.atividade;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.session.dominio.checkin.CheckInId;

public class RegistroAtividade {

    private final RegistroAtividadeId id;
    private final CheckInId checkInId;
    private final double distancia;
    private final long duracaoSegundos;
    private final Intensidade intensidade;
    private final double xpCalculado;

    public RegistroAtividade(CheckInId checkInId, double distancia, long duracaoSegundos,
                             Intensidade intensidade) {
        id = null;
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");
        isTrue(distancia > 0, "A distancia deve ser positiva");
        isTrue(duracaoSegundos > 0, "A duracao deve ser positiva");
        notNull(intensidade, "A intensidade nao pode ser nula");
        this.checkInId = checkInId;
        this.distancia = distancia;
        this.duracaoSegundos = duracaoSegundos;
        this.intensidade = intensidade;
        this.xpCalculado = distancia * intensidade.getMultiplicador();
    }

    public RegistroAtividade(RegistroAtividadeId id, CheckInId checkInId, double distancia,
                             long duracaoSegundos, Intensidade intensidade, double xpCalculado) {
        notNull(id, "O id do RegistroAtividade nao pode ser nulo");
        notNull(checkInId, "O id do CheckIn nao pode ser nulo");
        isTrue(distancia > 0, "A distancia deve ser positiva");
        isTrue(duracaoSegundos > 0, "A duracao deve ser positiva");
        notNull(intensidade, "A intensidade nao pode ser nula");
        this.id = id;
        this.checkInId = checkInId;
        this.distancia = distancia;
        this.duracaoSegundos = duracaoSegundos;
        this.intensidade = intensidade;
        this.xpCalculado = xpCalculado;
    }

    public RegistroAtividadeId getId() {
        return id;
    }

    public CheckInId getCheckInId() {
        return checkInId;
    }

    public double getDistancia() {
        return distancia;
    }

    public long getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public Intensidade getIntensidade() {
        return intensidade;
    }

    public double getXpCalculado() {
        return xpCalculado;
    }
}
