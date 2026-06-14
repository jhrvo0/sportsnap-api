package com.sportsnap.session.dominio.evento;

public interface EventoObservador<E> {
    void observarEvento(E evento);
}
