package com.sportsnap.session.infraestrutura.cliente;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GamificationCliente {

    private final RestTemplate restTemplate;
    private final String gamificationUrl;

    public GamificationCliente(RestTemplate restTemplate,
                                @Value("${sportsnap.services.gamification-url}") String gamificationUrl) {
        this.restTemplate = restTemplate;
        this.gamificationUrl = gamificationUrl;
    }

    public void notificarCheckIn(int atletaId, int sessaoId) {
        try {
            var url = gamificationUrl + "/api/atletas/" + atletaId + "/checkin-registrado";
            restTemplate.postForEntity(url, new CheckInNotificacao(sessaoId), Void.class);
        } catch (Exception e) {
            // Falha na notificacao nao deve cancelar o check-in
            System.err.println("Aviso: falha ao notificar gamification-service: " + e.getMessage());
        }
    }

    public record CheckInNotificacao(int sessaoId) {}
}
