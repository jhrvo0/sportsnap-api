package com.sportsnap.session.infraestrutura.evento;

import static org.apache.commons.lang3.Validate.notNull;

import com.sportsnap.session.dominio.evento.EventoBarramento;
import com.sportsnap.session.dominio.evento.EventoObservador;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.stereotype.Component;

@Component
public class EventoBarramentoSpring implements EventoBarramento {

    private final ApplicationEventPublisher publisher;
    private final ApplicationEventMulticaster multicaster;

    public EventoBarramentoSpring(ApplicationEventPublisher publisher,
                                   ApplicationEventMulticaster multicaster) {
        this.publisher = publisher;
        this.multicaster = multicaster;
    }

    @Override
    public <E> void adicionar(EventoObservador<E> observador, Class<E> tipoEvento) {
        notNull(observador, "O observador nao pode ser nulo");
        notNull(tipoEvento, "O tipo de evento nao pode ser nulo");
        multicaster.addApplicationListener((ApplicationListener<org.springframework.context.PayloadApplicationEvent<E>>) event -> {
            if (tipoEvento.isInstance(event.getPayload())) {
                observador.observarEvento(tipoEvento.cast(event.getPayload()));
            }
        });
    }

    @Override
    public void postar(Object evento) {
        notNull(evento, "O evento nao pode ser nulo");
        publisher.publishEvent(evento);
    }
}
