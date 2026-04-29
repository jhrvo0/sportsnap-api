package com.sportsnap.marketplace.dominio.sugestao;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

public class JanelaCheckIn {

    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public JanelaCheckIn(LocalDateTime inicio, LocalDateTime fim) {
        notNull(inicio, "O inicio da janela nao pode ser nulo");
        notNull(fim, "O fim da janela nao pode ser nulo");
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("O fim da janela nao pode ser anterior ao inicio");
        }
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
}
