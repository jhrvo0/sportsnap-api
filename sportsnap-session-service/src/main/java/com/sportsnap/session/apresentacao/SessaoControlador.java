package com.sportsnap.session.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sportsnap.session.aplicacao.sessao.SessaoResumo;
import com.sportsnap.session.aplicacao.sessao.SessaoServicoAplicacao;
import com.sportsnap.session.dominio.sessao.Periodo;
import com.sportsnap.session.dominio.sessao.SessaoId;
import com.sportsnap.session.dominio.sessao.SessaoServico;
import com.sportsnap.session.dominio.spot.SpotId;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/sessoes")
public class SessaoControlador {

    @Autowired private SessaoServico sessaoServico;
    @Autowired private SessaoServicoAplicacao sessaoServicoAplicacao;

    @GetMapping
    public List<SessaoResumo> listar(
            @RequestParam(required = false) Integer spotId,
            @RequestParam(required = false) Boolean ativas) {
        if (spotId != null) {
            return sessaoServico.listarPorSpot(new SpotId(spotId)).stream()
                .filter(s -> ativas == null || !ativas || s.estaAtiva(LocalDateTime.now()))
                .map(SessaoDtoResumo::new)
                .map(SessaoResumo.class::cast)
                .toList();
        }
        if (Boolean.TRUE.equals(ativas)) {
            return sessaoServico.listarAtivas().stream()
                .map(SessaoDtoResumo::new)
                .map(SessaoResumo.class::cast)
                .toList();
        }
        return sessaoServicoAplicacao.pesquisarResumos();
    }

    @PostMapping
    public void criar(@RequestBody SessaoDto dto) {
        var periodo = new Periodo(dto.inicio, dto.fim);
        sessaoServico.cadastrar(new SpotId(dto.spotId), periodo, dto.descricao);
    }

    @PutMapping("/{id}")
    public void atualizar(@PathVariable int id, @RequestBody SessaoDto dto) {
        var periodo = new Periodo(dto.inicio, dto.fim);
        sessaoServico.atualizar(new SessaoId(id), new SpotId(dto.spotId), periodo, dto.descricao);
    }

    @PostMapping("/{id}/cancelar")
    public void cancelar(@PathVariable int id) {
        sessaoServico.cancelar(new SessaoId(id));
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable int id) {
        sessaoServico.remover(new SessaoId(id));
    }

    public static class SessaoDto {
        public int spotId;
        public LocalDateTime inicio;
        public LocalDateTime fim;
        public String descricao;
    }

    private record SessaoDtoResumo(
        int getId,
        int getSpotId,
        LocalDateTime getPeriodoInicio,
        LocalDateTime getPeriodoFim,
        String getDescricao,
        boolean isCancelada
    ) implements SessaoResumo {
        SessaoDtoResumo(com.sportsnap.session.dominio.sessao.Sessao sessao) {
            this(
                sessao.getId().getId(),
                sessao.getSpotId().getId(),
                sessao.getPeriodo().getInicio(),
                sessao.getPeriodo().getFim(),
                sessao.getDescricao(),
                sessao.isCancelada()
            );
        }
    }
}
