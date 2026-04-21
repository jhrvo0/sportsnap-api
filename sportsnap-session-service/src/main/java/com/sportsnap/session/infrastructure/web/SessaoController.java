package com.sportsnap.session.infrastructure.web;

import com.sportsnap.session.domain.entities.CheckIn;
import com.sportsnap.session.domain.entities.RegistroDeAtividade;
import com.sportsnap.session.domain.entities.Session;
import com.sportsnap.session.domain.entities.Spot;
import com.sportsnap.session.domain.usecases.MotorDeMatchAutomatico;
import com.sportsnap.session.domain.usecases.ValidarCheckIn;
import com.sportsnap.session.infrastructure.persistence.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessoes")
public class SessaoController {

    private final JpaSpotRepository spotRepository;
    private final JpaSessionRepository sessionRepository;
    private final JpaCheckInRepository checkInRepository;
    private final JpaRegistroDeAtividadeRepository registroRepository;
    private final ValidarCheckIn validarCheckIn;
    private final MotorDeMatchAutomatico motorDeMatch;

    public SessaoController(JpaSpotRepository spotRepository,
                             JpaSessionRepository sessionRepository,
                             JpaCheckInRepository checkInRepository,
                             JpaRegistroDeAtividadeRepository registroRepository,
                             ValidarCheckIn validarCheckIn,
                             MotorDeMatchAutomatico motorDeMatch) {
        this.spotRepository = spotRepository;
        this.sessionRepository = sessionRepository;
        this.checkInRepository = checkInRepository;
        this.registroRepository = registroRepository;
        this.validarCheckIn = validarCheckIn;
        this.motorDeMatch = motorDeMatch;
    }

    @PostMapping("/spots")
    public ResponseEntity<Spot> criarSpot(@RequestBody Map<String, String> body) {
        Spot spot = new Spot(
                body.get("nome"),
                Double.parseDouble(body.get("latitude")),
                Double.parseDouble(body.get("longitude")),
                body.get("descricao")
        );
        return ResponseEntity.ok(spotRepository.save(spot));
    }

    @GetMapping("/spots")
    public ResponseEntity<List<Spot>> listarSpots() {
        return ResponseEntity.ok(spotRepository.findAll());
    }

    @PostMapping("/spots/{spotId}/sessions")
    public ResponseEntity<Session> criarSession(@PathVariable Long spotId,
                                                 @RequestBody Map<String, String> body) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Spot nao encontrado"));
        Session session = new Session(
                LocalDateTime.parse(body.get("inicio")),
                LocalDateTime.parse(body.get("fim")),
                spot
        );
        return ResponseEntity.ok(sessionRepository.save(session));
    }

    @GetMapping("/spots/{spotId}/sessions")
    public ResponseEntity<List<Session>> listarSessions(@PathVariable Long spotId) {
        return ResponseEntity.ok(sessionRepository.findBySpotId(spotId));
    }

    @PostMapping("/sessions/{sessionId}/checkin")
    public ResponseEntity<Map<String, String>> realizarCheckIn(@PathVariable Long sessionId,
                                                                @RequestBody Map<String, String> body) {
        try {
            Long atletaId = Long.parseLong(body.get("atletaId"));
            Double latitude = Double.parseDouble(body.get("latitude"));
            Double longitude = Double.parseDouble(body.get("longitude"));
            validarCheckIn.executar(atletaId, sessionId, latitude, longitude);
            return ResponseEntity.ok(Map.of("mensagem", "Check-in realizado com sucesso"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/sessions/{sessionId}/checkins")
    public ResponseEntity<List<CheckIn>> listarCheckIns(@PathVariable Long sessionId) {
        return ResponseEntity.ok(checkInRepository.findBySessionId(sessionId));
    }

    @PostMapping("/checkins/{checkInId}/atividade")
    public ResponseEntity<RegistroDeAtividade> registrarAtividade(@PathVariable Long checkInId,
                                                                    @RequestBody Map<String, String> body) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("CheckIn nao encontrado"));
        RegistroDeAtividade registro = new RegistroDeAtividade(
                Double.parseDouble(body.get("distancia")),
                Long.parseLong(body.get("duracaoSegundos")),
                Double.parseDouble(body.get("intensidade")),
                checkIn
        );
        return ResponseEntity.ok(registroRepository.save(registro));
    }

    @GetMapping("/sessions/{sessionId}/match")
    public ResponseEntity<List<Long>> executarMatch(@PathVariable Long sessionId) {
        return ResponseEntity.ok(motorDeMatch.executar(sessionId));
    }
}
