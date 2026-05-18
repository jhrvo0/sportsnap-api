package com.sportsnap.session.dominio.evento;

public interface EventoBarramento {

    <E> void adicionar(EventoObservador<E> observador, Class<E> tipoEvento);

    void postar(Object evento);
}
