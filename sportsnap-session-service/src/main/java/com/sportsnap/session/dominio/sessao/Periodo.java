package com.sportsnap.session.dominio.sessao;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

public class Periodo {

    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public Periodo(LocalDateTime inicio, LocalDateTime fim) {
        notNull(inicio, "O inicio do periodo nao pode ser nulo");
        notNull(fim, "O fim do periodo nao pode ser nulo");
        isTrue(!fim.isBefore(inicio), "O fim do periodo nao pode ser anterior ao inicio");
        this.inicio = inicio;
        this.fim = fim;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public boolean contem(LocalDateTime instante) {
        notNull(instante, "O instante nao pode ser nulo");
        return !instante.isBefore(inicio) && !instante.isAfter(fim);
    }

    public boolean terminou(LocalDateTime agora) {
        notNull(agora, "O instante nao pode ser nulo");
        return agora.isAfter(fim);
    }

    public boolean jaIniciou(LocalDateTime agora) {
        notNull(agora, "O instante nao pode ser nulo");
        return !agora.isBefore(inicio);
    }
}
