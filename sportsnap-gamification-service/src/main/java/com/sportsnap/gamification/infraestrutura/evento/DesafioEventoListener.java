package com.sportsnap.gamification.infraestrutura.evento;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sportsnap.gamification.dominio.desafio.MotorDesafios;
import com.sportsnap.gamification.dominio.sincronizacao.SincronizacaoServico.CartaSincronizadaEvento;

/**
 * Adaptador de infraestrutura que liga o barramento Spring ao MotorDesafios:
 * ao receber a CartaSincronizadaEvento, dispara o avanco dos progressos (RN24).
 * Mantem o dominio livre de anotacoes do framework.
 */
@Component
public class DesafioEventoListener {

    private final MotorDesafios motorDesafios;

    public DesafioEventoListener(MotorDesafios motorDesafios) {
        this.motorDesafios = motorDesafios;
    }

    @EventListener
    public void aoSincronizarCarta(CartaSincronizadaEvento evento) {
        motorDesafios.aoSincronizar(evento);
    }
}
