package com.sportsnap.gamification.apresentacao;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.reveal.Orcamento;
import com.sportsnap.gamification.dominio.reveal.RegistroSincronizacao;
import com.sportsnap.gamification.dominio.reveal.RevealServico;
import com.sportsnap.gamification.dominio.reveal.Simulacao;

@RestController
@RequestMapping("/api/atletas/{id}/reveal")
public class RevealControlador {

    @Autowired private RevealServico revealServico;

    @GetMapping("/orcamento")
    public OrcamentoDto orcamento(@PathVariable int id) {
        Orcamento orcamento = revealServico.iniciar(new AtletaId(id));
        return new OrcamentoDto(orcamento.getPontosDisponiveis(),
            orcamento.getTier().name(), orcamento.getXpLatente());
    }

    @PostMapping("/simular")
    public SimulacaoDto simular(@PathVariable int id, @RequestBody AlocacaoDto dto) {
        Simulacao s = revealServico.simular(new AtletaId(id), dto.alocacao);
        return new SimulacaoDto(s.getOverallAnterior(), s.getOverallResultante(),
            s.getTierAnterior().name(), s.getTierResultante().name(),
            s.isHaveriaPromocao(), s.getCustoTotal(), s.getSaldoRestante());
    }

    @PostMapping("/confirmar")
    public RegistroSincronizacaoDto confirmar(@PathVariable int id, @RequestBody AlocacaoDto dto) {
        RegistroSincronizacao r = revealServico.confirmar(new AtletaId(id), dto.alocacao);
        return new RegistroSincronizacaoDto(r.getOrcamentoPontos(), r.getCustoTotal(),
            r.getOverallAnterior(), r.getOverallNovo(), r.getVariacaoOverall());
    }

    public static class AlocacaoDto {
        public Map<String, Integer> alocacao;
    }

    public record OrcamentoDto(int pontosDisponiveis, String tier, double xpLatente) {}

    public record SimulacaoDto(double overallAnterior, double overallResultante, String tierAnterior,
                               String tierResultante, boolean haveriaPromocao, int custoTotal,
                               int saldoRestante) {}

    public record RegistroSincronizacaoDto(int orcamentoPontos, int custoTotal, double overallAnterior,
                                           double overallNovo, double variacaoOverall) {}
}
