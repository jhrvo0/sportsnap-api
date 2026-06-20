package com.sportsnap.session.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.sportsnap.session.dominio.atividade.AtividadeServico;
import com.sportsnap.session.dominio.atividade.RegistroAtividade;
import com.sportsnap.session.dominio.atividade.RegistroAtividadeId;
import com.sportsnap.session.dominio.atividade.Intensidade;
import com.sportsnap.session.dominio.atividade.AnaliseAtividadeServico;
import com.sportsnap.session.dominio.atividade.AnaliseEvolucao;
import com.sportsnap.session.dominio.atleta.AtletaId;
import com.sportsnap.session.dominio.checkin.CheckInId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/atividades")
public class AtividadeControlador {

    @Autowired private AtividadeServico atividadeServico;
    @Autowired private AnaliseAtividadeServico analiseAtividadeServico;

    @PostMapping
    public RegistroAtividadeDto registrar(@RequestBody RegistroAtividadeDto dto) {
        var data = dto.data != null ? dto.data : LocalDateTime.now();
        RegistroAtividade salvo;

        if (dto.checkInId != null) {
            var intensidade = dto.intensidade != null ? Intensidade.apartirDeTexto(dto.intensidade) : null;
            salvo = atividadeServico.registrarComCheckIn(
                new AtletaId(dto.atletaId), new CheckInId(dto.checkInId),
                dto.esporte, data, dto.distancia, dto.duracaoSegundos,
                intensidade, dto.xpCalculado, dto.esforcoPercebido,
                dto.observacoes, dto.origemRegistro != null ? dto.origemRegistro : "CHECKIN",
                dto.metricas
            );
        } else {
            salvo = atividadeServico.registrarManual(
                new AtletaId(dto.atletaId), dto.esporte, data,
                dto.distancia, dto.duracaoSegundos,
                dto.esforcoPercebido, dto.observacoes, dto.metricas
            );
        }
        return paraDto(salvo);
    }

    @GetMapping
    public List<RegistroAtividadeDto> listar(
            @RequestParam int atletaId,
            @RequestParam(required = false) String esporte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        
        List<RegistroAtividade> lista;
        if (esporte != null && inicio != null && fim != null) {
            lista = atividadeServico.listarPorAtletaEsporteEPeriodo(new AtletaId(atletaId), esporte, inicio, fim);
        } else if (esporte != null) {
            lista = atividadeServico.listarPorAtletaEEsporte(new AtletaId(atletaId), esporte);
        } else {
            lista = atividadeServico.listarPorAtleta(new AtletaId(atletaId));
        }
        return lista.stream().map(this::paraDto).toList();
    }

    @GetMapping("/{id}")
    public RegistroAtividadeDto obter(@PathVariable int id) {
        var registro = atividadeServico.obter(new RegistroAtividadeId(id))
            .orElseThrow(() -> new IllegalArgumentException("Atividade nao encontrada: " + id));
        return paraDto(registro);
    }

    @PutMapping("/{id}")
    public RegistroAtividadeDto atualizar(@PathVariable int id, @RequestBody RegistroAtividadeDto dto) {
        var intensidade = dto.intensidade != null ? Intensidade.apartirDeTexto(dto.intensidade) : null;
        var atualizado = atividadeServico.atualizar(
            new RegistroAtividadeId(id),
            dto.esporte,
            dto.data,
            dto.distancia,
            dto.duracaoSegundos,
            intensidade,
            dto.xpCalculado,
            dto.esforcoPercebido,
            dto.observacoes,
            dto.metricas
        );
        return paraDto(atualizado);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable int id) {
        atividadeServico.remover(new RegistroAtividadeId(id));
    }

    @GetMapping("/analise")
    public AnaliseEvolucao obterAnalise(
            @RequestParam int atletaId,
            @RequestParam String esporte,
            @RequestParam(defaultValue = "30d") String periodo) {
        
        int dias = switch (periodo.toLowerCase().trim()) {
            case "7d" -> 7;
            case "90d" -> 90;
            default -> 30;
        };

        return analiseAtividadeServico.gerarAnalise(new AtletaId(atletaId), esporte, dias);
    }

    private RegistroAtividadeDto paraDto(RegistroAtividade r) {
        var dto = new RegistroAtividadeDto();
        dto.id = r.getId() != null ? r.getId().getId() : null;
        dto.atletaId = r.getAtletaId() != null ? r.getAtletaId().getId() : 0;
        dto.checkInId = r.getCheckInId() != null ? r.getCheckInId().getId() : null;
        dto.esporte = r.getEsporte();
        dto.data = r.getData();
        dto.distancia = r.getDistancia();
        dto.duracaoSegundos = r.getDuracaoSegundos();
        dto.intensidade = r.getIntensidade() != null ? r.getIntensidade().name() : null;
        dto.xpCalculado = r.getXpCalculado();
        dto.esforcoPercebido = r.getEsforcoPercebido();
        dto.observacoes = r.getObservacoes();
        dto.origemRegistro = r.getOrigemRegistro();
        dto.metricas = r.getMetricas();
        dto.ritmoMedio = r.getRitmoMedio();
        dto.caloriasEstimadas = r.getCaloriasEstimadas();
        dto.criadoEm = r.getCriadoEm();
        dto.atualizadoEm = r.getAtualizadoEm();
        return dto;
    }

    public static class RegistroAtividadeDto {
        public Integer id;
        public int atletaId;
        public Integer checkInId;
        public String esporte;
        public LocalDateTime data;
        public double distancia;
        public long duracaoSegundos;
        public String intensidade;
        public double xpCalculado;
        public Integer esforcoPercebido;
        public String observacoes;
        public String origemRegistro;
        public String metricas;
        public Double ritmoMedio;
        public Double caloriasEstimadas;
        public LocalDateTime criadoEm;
        public LocalDateTime updated_at; // backwards compatibility
        public LocalDateTime atualizadoEm;
    }
}
