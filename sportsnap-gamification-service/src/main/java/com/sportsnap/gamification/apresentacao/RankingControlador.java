package com.sportsnap.gamification.apresentacao;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.aplicacao.ranking.CartaResumo;
import com.sportsnap.gamification.aplicacao.ranking.RankingServicoAplicacao;
import com.sportsnap.gamification.dominio.atleta.AtletaId;
import com.sportsnap.gamification.dominio.ranking.RankingServico;
import com.sportsnap.gamification.dominio.ranking.RankingServico.EntradaRanking;

@RestController
@RequestMapping("/api/ranking")
public class RankingControlador {

    @Autowired private RankingServicoAplicacao rankingServicoAplicacao;
    @Autowired private RankingServico rankingServico;
    @Autowired private ExecutorService rankingExecutor;

    @GetMapping
    public List<CartaResumo> ranking() {
        return rankingServicoAplicacao.pesquisarRankingGlobal();
    }

    @PostMapping("/calcular-concorrente")
    public List<EntradaRanking> calcularConcorrente(@RequestBody List<Integer> atletaIds) {
        var ids = atletaIds.stream().map(AtletaId::new).toList();
        return rankingServico.calcularRankingConcorrente(ids, rankingExecutor);
    }
}
