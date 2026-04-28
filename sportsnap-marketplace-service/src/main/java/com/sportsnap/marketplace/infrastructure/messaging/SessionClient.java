package com.sportsnap.marketplace.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Cliente REST para comunicacao com o Session Service.
 * Consulta sessions e check-ins para cruzamento com fotos (Motor de Match).
 */
@Component
public class SessionClient {

    private final RestTemplate restTemplate;
    private final String sessionUrl;

    public SessionClient(@Value("${sportsnap.session.url:http://localhost:8083}") String sessionUrl) {
        this.restTemplate = new RestTemplate();
        this.sessionUrl = sessionUrl;
    }

    @SuppressWarnings("unchecked")
    public List<Long> buscarAtletasComMatch(Long sessionId) {
        try {
            return restTemplate.getForObject(
                    sessionUrl + "/api/sessoes/sessions/" + sessionId + "/match",
                    List.class
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscarCheckIns(Long sessionId) {
        try {
            return restTemplate.getForObject(
                    sessionUrl + "/api/sessoes/sessions/" + sessionId + "/checkins",
                    List.class
            );
        } catch (Exception e) {
            return List.of();
        }
    }
}
