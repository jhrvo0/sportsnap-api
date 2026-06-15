package com.sportsnap.gamification.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.dominio.analise.AnaliseServico;
import com.sportsnap.gamification.dominio.analise.DadosRadar;
import com.sportsnap.gamification.dominio.analise.PercentilAtributo;
import com.sportsnap.gamification.dominio.analise.Projecao;
import com.sportsnap.gamification.dominio.atleta.AtletaId;

@RestController
@RequestMapping("/api/atletas/{id}/analise")
public class AnaliseControlador {

    @Autowired private AnaliseServico analiseServico;

    @GetMapping("/percentil")
    public PercentilDto percentil(@PathVariable int id, @RequestParam String atributo) {
        PercentilAtributo p = analiseServico.percentil(new AtletaId(id), atributo);
        return new PercentilDto(p.getAtributo(), p.getValor(), p.getPercentil());
    }

    @GetMapping("/similares")
    public List<SimilarDto> similares(@PathVariable int id, @RequestParam String modalidade,
                                      @RequestParam(defaultValue = "5") int n) {
        return analiseServico.similares(new AtletaId(id), n, modalidade).stream()
            .map(r -> new SimilarDto(r.getAtletaId().getId(), r.getDistancia()))
            .toList();
    }

    @GetMapping("/forca-fraqueza")
    public List<ForcaFraquezaDto> forcaFraqueza(@PathVariable int id, @RequestParam String modalidade) {
        return analiseServico.forcaFraqueza(new AtletaId(id), modalidade).stream()
            .map(f -> new ForcaFraquezaDto(f.getAtributo(), f.getValor(), f.getMedia(),
                f.getClassificacao().name()))
            .toList();
    }

    @GetMapping("/radar")
    public DadosRadar radar(@PathVariable int id) {
        return analiseServico.radar(new AtletaId(id));
    }

    @GetMapping("/projecao")
    public ProjecaoDto projecao(@PathVariable int id) {
        Projecao p = analiseServico.projecao(new AtletaId(id));
        return new ProjecaoDto(p.getOverallAtual(), p.getTendencia(), p.getOverallProjetado());
    }

    public record PercentilDto(String atributo, double valor, double percentil) {}

    public record SimilarDto(int atletaId, double distancia) {}

    public record ForcaFraquezaDto(String atributo, double valor, double media, String classificacao) {}

    public record ProjecaoDto(double overallAtual, double tendencia, double overallProjetado) {}
}
