package com.sportsnap.marketplace.infraestrutura.cliente;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class SessionCliente {

    private final RestTemplate restTemplate;
    private final String sessionUrl;

    public SessionCliente(RestTemplate restTemplate,
                           @Value("${sportsnap.services.session-url}") String sessionUrl) {
        this.restTemplate = restTemplate;
        this.sessionUrl = sessionUrl;
    }

    public boolean sessaoExiste(int sessaoId) {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(
                sessionUrl + "/api/sessoes/" + sessaoId, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            // Se session-service indisponivel, assume valido (fail-open)
            System.err.println("Aviso: falha ao validar sessao: " + e.getMessage());
            return true;
        }
    }
}
