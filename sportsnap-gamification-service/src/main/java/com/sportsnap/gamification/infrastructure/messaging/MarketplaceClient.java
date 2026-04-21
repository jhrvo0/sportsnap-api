package com.sportsnap.gamification.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Cliente REST para comunicacao com o Marketplace Service.
 * Verifica se o atleta possui uma LicencaDeImagem valida para sincronizacao.
 */
@Component
public class MarketplaceClient {

    private final RestTemplate restTemplate;
    private final String marketplaceUrl;

    public MarketplaceClient(@Value("${sportsnap.marketplace.url:http://localhost:8082}") String marketplaceUrl) {
        this.restTemplate = new RestTemplate();
        this.marketplaceUrl = marketplaceUrl;
    }

    @SuppressWarnings("unchecked")
    public boolean atletaPossuiLicencaValida(Long atletaId) {
        try {
            List<?> licencas = restTemplate.getForObject(
                    marketplaceUrl + "/api/marketplace/atletas/" + atletaId + "/licencas",
                    List.class
            );
            return licencas != null && !licencas.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
