package com.sportsnap.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sportsnap.gamification.url:http://localhost:8081}")
    private String gamificationUrl;

    @Value("${sportsnap.marketplace.url:http://localhost:8082}")
    private String marketplaceUrl;

    @Value("${sportsnap.session.url:http://localhost:8083}")
    private String sessionUrl;

    // === GAMIFICATION ===

    @SuppressWarnings("unchecked")
    public Map<String, Object> criarAtleta(String nome, String email) {
        return restTemplate.postForObject(
                gamificationUrl + "/api/gamificacao/atletas",
                Map.of("nome", nome, "email", email),
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarAtleta(Long id) {
        try {
            return restTemplate.getForObject(gamificationUrl + "/api/gamificacao/atletas/" + id, Map.class);
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarAtletas() {
        try {
            return restTemplate.getForObject(gamificationUrl + "/api/gamificacao/atletas", List.class);
        } catch (Exception e) { return List.of(); }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> sincronizarCarta(Long atletaId) {
        return restTemplate.postForObject(
                gamificationUrl + "/api/gamificacao/atletas/" + atletaId + "/sincronizar",
                null, Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarCarta(Long atletaId) {
        try {
            return restTemplate.getForObject(gamificationUrl + "/api/gamificacao/atletas/" + atletaId + "/carta", Map.class);
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarStatusPotencial(Long atletaId) {
        try {
            return restTemplate.getForObject(gamificationUrl + "/api/gamificacao/atletas/" + atletaId + "/status-potencial", Map.class);
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscarRanking() {
        try {
            return restTemplate.getForObject(gamificationUrl + "/api/gamificacao/ranking", List.class);
        } catch (Exception e) { return List.of(); }
    }

    // === MARKETPLACE ===

    @SuppressWarnings("unchecked")
    public Map<String, Object> criarFotografo(String nome, String email) {
        return restTemplate.postForObject(
                marketplaceUrl + "/api/marketplace/fotografos",
                Map.of("nome", nome, "email", email),
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarLotes(Long fotografoId) {
        try {
            return restTemplate.getForObject(marketplaceUrl + "/api/marketplace/fotografos/" + fotografoId + "/lotes", List.class);
        } catch (Exception e) { return List.of(); }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> criarLote(Long fotografoId, Long sessionId, Long spotId) {
        return restTemplate.postForObject(
                marketplaceUrl + "/api/marketplace/fotografos/" + fotografoId + "/lotes",
                Map.of("sessionId", sessionId, "spotId", spotId),
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadFotos(Long loteId, List<String> caminhos) {
        return restTemplate.postForObject(
                marketplaceUrl + "/api/marketplace/lotes/" + loteId + "/upload",
                Map.of("caminhos", caminhos),
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarFotos(Long loteId) {
        try {
            return restTemplate.getForObject(marketplaceUrl + "/api/marketplace/lotes/" + loteId + "/fotos", List.class);
        } catch (Exception e) { return List.of(); }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> comprarLicenca(Long fotoId, Long atletaId) {
        return restTemplate.postForObject(
                marketplaceUrl + "/api/marketplace/fotos/" + fotoId + "/comprar",
                Map.of("atletaId", atletaId),
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarLicencas(Long atletaId) {
        try {
            return restTemplate.getForObject(marketplaceUrl + "/api/marketplace/atletas/" + atletaId + "/licencas", List.class);
        } catch (Exception e) { return List.of(); }
    }

    // === SESSION ===

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarSpots() {
        try {
            return restTemplate.getForObject(sessionUrl + "/api/sessoes/spots", List.class);
        } catch (Exception e) { return List.of(); }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> criarSpot(String nome, String latitude, String longitude, String descricao) {
        return restTemplate.postForObject(
                sessionUrl + "/api/sessoes/spots",
                Map.of("nome", nome, "latitude", latitude, "longitude", longitude, "descricao", descricao),
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarSessions(Long spotId) {
        try {
            return restTemplate.getForObject(sessionUrl + "/api/sessoes/spots/" + spotId + "/sessions", List.class);
        } catch (Exception e) { return List.of(); }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> realizarCheckIn(Long sessionId, Long atletaId, String latitude, String longitude) {
        return restTemplate.postForObject(
                sessionUrl + "/api/sessoes/sessions/" + sessionId + "/checkin",
                Map.of("atletaId", String.valueOf(atletaId), "latitude", latitude, "longitude", longitude),
                Map.class
        );
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarCheckIns(Long sessionId) {
        try {
            return restTemplate.getForObject(sessionUrl + "/api/sessoes/sessions/" + sessionId + "/checkins", List.class);
        } catch (Exception e) { return List.of(); }
    }
}
