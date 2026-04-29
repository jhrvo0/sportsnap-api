package com.sportsnap.gamification.infraestrutura.evento;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.gamification.dominio.evento.EventoBarramento;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventoBarramentoSpring implements EventoBarramento {

    private final ApplicationEventPublisher publisher;

    public EventoBarramentoSpring(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void postar(Object evento) {
        notNull(evento, "O evento nao pode ser nulo");
        publisher.publishEvent(evento);
    }
}
