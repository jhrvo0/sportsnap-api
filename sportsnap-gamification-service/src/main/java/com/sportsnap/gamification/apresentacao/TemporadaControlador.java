package com.sportsnap.gamification.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.competicao.Temporada;
import com.sportsnap.gamification.dominio.competicao.TemporadaRepositorio;
import com.sportsnap.gamification.dominio.competicao.TemporadaServico;

@RestController
@RequestMapping("/api/temporadas")
public class TemporadaControlador {

    @Autowired private TemporadaServico temporadaServico;
    @Autowired private TemporadaRepositorio temporadaRepositorio;

    @GetMapping
    public List<TemporadaDto> listar() {
        return temporadaRepositorio.listarTodas().stream().map(this::paraDto).toList();
    }

    @PostMapping
    public TemporadaDto criar(@RequestBody TemporadaRequest req) {
        return paraDto(temporadaServico.criar(req.modalidade, req.inicio, req.fim));
    }

    @PostMapping("/{id}/cancelar")
    public TemporadaDto cancelar(@PathVariable int id) {
        return paraDto(temporadaServico.cancelar(id, LocalDateTime.now()));
    }

    @PostMapping("/{id}/encerrar")
    public TemporadaDto encerrar(@PathVariable int id) {
        return paraDto(temporadaServico.encerrar(id, LocalDateTime.now()));
    }

    private TemporadaDto paraDto(Temporada t) {
        return new TemporadaDto(t.getId(), t.getModalidade(), t.getInicio(), t.getFim(),
            t.getStatus().name(), t.getSnapshotFinal().size());
    }

    public static class TemporadaRequest {
        public String modalidade;
        public LocalDateTime inicio;
        public LocalDateTime fim;
    }

    public record TemporadaDto(Integer id, String modalidade, LocalDateTime inicio, LocalDateTime fim,
                               String status, int tamanhoSnapshot) {}
}
