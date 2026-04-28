package com.sportsnap.gamification.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Cliente REST para comunicacao com o Session Service.
 * Consulta check-ins e registros de atividade do atleta.
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
    public List<Map<String, Object>> buscarCheckInsDoAtleta(Long atletaId) {
        try {
            return restTemplate.getForObject(
                    sessionUrl + "/api/sessoes/atletas/" + atletaId + "/checkins",
                    List.class
            );
        } catch (Exception e) {
            return List.of();
        }
    }
}
