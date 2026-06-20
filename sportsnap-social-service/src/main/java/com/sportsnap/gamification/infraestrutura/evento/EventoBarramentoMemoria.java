package com.sportsnap.gamification.infraestrutura.evento;

import com.sportsnap.gamification.dominio.evento.EventoBarramento;

public class EventoBarramentoMemoria implements EventoBarramento {

    @Override
    public void postar(Object evento) {
        // no-op for unit tests
    }
}
