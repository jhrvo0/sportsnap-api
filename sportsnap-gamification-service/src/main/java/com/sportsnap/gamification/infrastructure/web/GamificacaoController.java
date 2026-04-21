package com.sportsnap.gamification.infrastructure.web;

import com.sportsnap.gamification.domain.entities.Atleta;
import com.sportsnap.gamification.domain.entities.CartaOficial;
import com.sportsnap.gamification.domain.entities.StatusPotencial;
import com.sportsnap.gamification.domain.usecases.CalcularOverall;
import com.sportsnap.gamification.domain.usecases.SincronizarCartaAtleta;
import com.sportsnap.gamification.infrastructure.persistence.JpaAtletaRepository;
import com.sportsnap.gamification.infrastructure.persistence.JpaCartaOficialRepository;
import com.sportsnap.gamification.infrastructure.persistence.JpaStatusPotencialRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gamificacao")
public class GamificacaoController {

    private final JpaAtletaRepository atletaRepository;
    private final JpaCartaOficialRepository cartaOficialRepository;
    private final JpaStatusPotencialRepository statusPotencialRepository;
    private final SincronizarCartaAtleta sincronizarCartaAtleta;
    private final CalcularOverall calcularOverall;

    public GamificacaoController(JpaAtletaRepository atletaRepository,
                                  JpaCartaOficialRepository cartaOficialRepository,
                                  JpaStatusPotencialRepository statusPotencialRepository,
                                  SincronizarCartaAtleta sincronizarCartaAtleta,
                                  CalcularOverall calcularOverall) {
        this.atletaRepository = atletaRepository;
        this.cartaOficialRepository = cartaOficialRepository;
        this.statusPotencialRepository = statusPotencialRepository;
        this.sincronizarCartaAtleta = sincronizarCartaAtleta;
        this.calcularOverall = calcularOverall;
    }

    @PostMapping("/atletas")
    public ResponseEntity<Atleta> criarAtleta(@RequestBody Map<String, String> body) {
        Atleta atleta = new Atleta(body.get("nome"), body.get("email"));
        atleta = atletaRepository.save(atleta);
        return ResponseEntity.ok(atleta);
    }

    @GetMapping("/atletas/{id}")
    public ResponseEntity<Atleta> buscarAtleta(@PathVariable Long id) {
        return atletaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/atletas")
    public ResponseEntity<List<Atleta>> listarAtletas() {
        return ResponseEntity.ok(atletaRepository.findAll());
    }

    @PostMapping("/atletas/{id}/sincronizar")
    public ResponseEntity<Map<String, Object>> sincronizar(@PathVariable Long id) {
        try {
            sincronizarCartaAtleta.executar(id);
            Double novoOverall = calcularOverall.executar(id);
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Sincronizacao realizada com sucesso",
                    "overall", novoOverall
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/atletas/{id}/carta")
    public ResponseEntity<CartaOficial> buscarCarta(@PathVariable Long id) {
        return cartaOficialRepository.findByAtletaId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/atletas/{id}/status-potencial")
    public ResponseEntity<StatusPotencial> buscarStatusPotencial(@PathVariable Long id) {
        return statusPotencialRepository.findByAtletaId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<CartaOficial>> ranking() {
        return ResponseEntity.ok(cartaOficialRepository.findAllByOrderByOverallDesc());
    }
}
