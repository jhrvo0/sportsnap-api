package com.sportsnap.marketplace.application;

import com.sportsnap.marketplace.domain.usecases.MarketplaceObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Pattern: Observer — Notifica via log quando eventos acontecem
@Component
public class LogNotificacaoObserver implements MarketplaceObserver {

    private static final Logger log = LoggerFactory.getLogger(LogNotificacaoObserver.class);

    @Override
    public void onFotoSugerida(Long atletaId, Long fotoId, String mensagem) {
        log.info("[NOTIFICACAO] Atleta {} — Foto sugerida {}: {}", atletaId, fotoId, mensagem);
    }

    @Override
    public void onLicencaAdquirida(Long atletaId, Long fotoId, String mensagem) {
        log.info("[NOTIFICACAO] Atleta {} — Licenca adquirida foto {}: {}", atletaId, fotoId, mensagem);
    }
}
