package com.sportsnap.marketplace.domain.usecases;

/**
 * Pattern: Observer
 *
 * Interface para observers que sao notificados quando ocorre um evento
 * no marketplace (ex: nova foto disponivel, licenca adquirida).
 */
public interface MarketplaceObserver {

    void onFotoSugerida(Long atletaId, Long fotoId, String mensagem);

    void onLicencaAdquirida(Long atletaId, Long fotoId, String mensagem);
}
