package com.sportsnap.session.bdd.steps;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ColetorDeEventos {

    private final List<Object> eventos = new ArrayList<>();

    @EventListener
    public void capturar(Object evento) {
        eventos.add(evento);
    }

    public List<Object> getEventos() {
        return Collections.unmodifiableList(eventos);
    }

    public void limpar() {
        eventos.clear();
    }
}
