package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.usecases.MarketplaceObserver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Pattern: Observer — Gerenciador de eventos do marketplace.
 *
 * Mantem uma lista de observers e os notifica quando eventos acontecem.
 * Utiliza CopyOnWriteArrayList para seguranca em cenarios concorrentes.
 */
@Component
public class MarketplaceEventPublisher {

    private final List<MarketplaceObserver> observers;

    public MarketplaceEventPublisher(List<MarketplaceObserver> observers) {
        this.observers = new CopyOnWriteArrayList<>(observers);
    }

    public void notificarFotoSugerida(Long atletaId, Long fotoId) {
        for (MarketplaceObserver observer : observers) {
            observer.onFotoSugerida(atletaId, fotoId,
                    "Nova foto disponivel para voce!");
        }
    }

    public void notificarLicencaAdquirida(Long atletaId, Long fotoId) {
        for (MarketplaceObserver observer : observers) {
            observer.onLicencaAdquirida(atletaId, fotoId,
                    "Licenca adquirida com sucesso! Voce pode sincronizar sua carta.");
        }
    }
}
