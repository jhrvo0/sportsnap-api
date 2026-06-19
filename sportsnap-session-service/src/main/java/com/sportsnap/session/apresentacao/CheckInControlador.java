package com.sportsnap.session.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckIn;
import com.sportsnap.session.dominio.checkin.CheckInId;
import com.sportsnap.session.dominio.checkin.CheckInServico;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.spot.Coordenada;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/checkins")
public class CheckInControlador {

    @Autowired
    private CheckInServico checkInServico;

    @PostMapping
    public CheckInDto realizar(@RequestBody CheckInDto dto) {
        var checkIn = checkInServico.realizar(
            new AtletaId(dto.atletaId),
            new SessaoId(dto.sessaoId),
            new Coordenada(dto.latitude, dto.longitude)
        );
        return paraDto(checkIn);
    }

    @GetMapping
    public List<CheckInDto> listar(
            @RequestParam(required = false) Integer atletaId,
            @RequestParam(required = false) Integer sessaoId) {
        
        List<CheckIn> list;
        if (atletaId != null) {
            list = checkInServico.listarPorAtleta(new AtletaId(atletaId));
        } else if (sessaoId != null) {
            list = checkInServico.listarPorSessao(new SessaoId(sessaoId));
        } else {
            list = List.of();
        }
        return list.stream().map(this::paraDto).toList();
    }

    @PostMapping("/{id}/cancelar")
    public void cancelar(@PathVariable int id) {
        checkInServico.cancelar(new CheckInId(id));
    }

    private CheckInDto paraDto(CheckIn c) {
        var dto = new CheckInDto();
        dto.id = c.getId() != null ? c.getId().getId() : null;
        dto.atletaId = c.getAtletaId().getId();
        dto.sessaoId = c.getSessaoId().getId();
        dto.horario = c.getHorario();
        dto.latitude = c.getCoordenada().getLatitude();
        dto.longitude = c.getCoordenada().getLongitude();
        dto.cancelado = c.isCancelado();
        dto.atividadeRegistrada = c.temAtividadeRegistrada();
        return dto;
    }

    public static class CheckInDto {
        public Integer id;
        public int atletaId;
        public int sessaoId;
        public LocalDateTime horario;
        public double latitude;
        public double longitude;
        public boolean cancelado;
        public boolean atividadeRegistrada;
    }
}
