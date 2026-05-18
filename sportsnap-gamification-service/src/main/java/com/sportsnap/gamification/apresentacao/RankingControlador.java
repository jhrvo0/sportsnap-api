package com.sportsnap.gamification.apresentacao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportsnap.gamification.aplicacao.ranking.CartaResumo;
import com.sportsnap.gamification.aplicacao.ranking.RankingServicoAplicacao;

@RestController
@RequestMapping("/api/ranking")
public class RankingControlador {

    @Autowired private RankingServicoAplicacao rankingServicoAplicacao;

    @GetMapping
    public List<CartaResumo> ranking() {
        return rankingServicoAplicacao.pesquisarRankingGlobal();
    }
}
