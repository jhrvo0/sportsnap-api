package com.sportsnap.gamification.apresentacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.competicao.CompeticaoServico;
import com.sportsnap.gamification.dominio.competicao.Confronto;
import com.sportsnap.gamification.dominio.competicao.PontuacaoRanking;

@RestController
@RequestMapping("/api/competicao")
public class CompeticaoControlador {

    @Autowired private CompeticaoServico competicaoServico;

    @PostMapping("/elegiveis/{id}")
    public PontuacaoDto registrar(@PathVariable int id) {
        return paraDto(competicaoServico.registrarElegivel(new AtletaId(id)));
    }

    @PostMapping("/confrontos")
    public ConfrontoDto resolver(@RequestBody ConfrontoRequest req) {
        Confronto c = competicaoServico.resolverConfronto(new AtletaId(req.atletaA),
            new AtletaId(req.atletaB), req.modalidade, LocalDateTime.now());
        return new ConfrontoDto(c.getVencedorId().getId(), c.getPerdedorId().getId(),
            c.getPrTransferida(), c.getTemporadaId());
    }

    @GetMapping("/classificacao")
    public List<PontuacaoDto> classificacao() {
        return competicaoServico.classificacao().stream().map(this::paraDto).toList();
    }

    @GetMapping("/posicao/{id}")
    public PosicaoDto posicao(@PathVariable int id) {
        return competicaoServico.consultarPosicao(new AtletaId(id))
            .map(p -> new PosicaoDto(p.getPosicao(), p.getLiga().name(), p.getPr(), true))
            .orElseGet(() -> new PosicaoDto(0, null, 0, false));
    }

    @GetMapping("/{id}/oponentes")
    public List<PontuacaoDto> oponentes(@PathVariable int id,
                                        @RequestParam(defaultValue = "5") int limite) {
        return competicaoServico.sugerirOponentes(new AtletaId(id), limite).stream()
            .map(this::paraDto).toList();
    }

    private PontuacaoDto paraDto(PontuacaoRanking p) {
        return new PontuacaoDto(p.getAtletaId().getId(), p.getPr(), p.getLiga().name());
    }

    public static class ConfrontoRequest {
        public int atletaA;
        public int atletaB;
        public String modalidade;
    }

    public record PontuacaoDto(int atletaId, double pr, String liga) {}

    public record ConfrontoDto(int vencedorId, int perdedorId, double prTransferida, int temporadaId) {}

    public record PosicaoDto(int posicao, String liga, double pr, boolean classificado) {}
}
