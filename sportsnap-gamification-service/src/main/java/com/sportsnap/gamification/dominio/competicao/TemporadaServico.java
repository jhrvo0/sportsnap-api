package com.sportsnap.gamification.dominio.competicao;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ciclo de vida das temporadas (RN36 a RN39): criacao com periodo valido e sem
 * sobreposicao na modalidade, cancelamento antes do inicio e encerramento com
 * snapshot final imutavel e soft-reset de PR.
 */
public class TemporadaServico {

    private static final double FATOR_SOFT_RESET = 0.5;

    private final TemporadaRepositorio temporadaRepositorio;
    private final PontuacaoRankingRepositorio pontuacaoRepositorio;

    public TemporadaServico(TemporadaRepositorio temporadaRepositorio,
                            PontuacaoRankingRepositorio pontuacaoRepositorio) {
        notNull(temporadaRepositorio, "O repositorio de Temporada nao pode ser nulo");
        notNull(pontuacaoRepositorio, "O repositorio de PontuacaoRanking nao pode ser nulo");
        this.temporadaRepositorio = temporadaRepositorio;
        this.pontuacaoRepositorio = pontuacaoRepositorio;
    }

    /** Cria uma temporada com periodo valido (RN36) e sem sobreposicao (RN37). */
    public Temporada criar(String modalidade, LocalDateTime inicio, LocalDateTime fim) {
        notBlank(modalidade, "A modalidade da temporada e obrigatoria");
        Temporada nova = new Temporada(modalidade, inicio, fim);
        boolean sobrepoe = temporadaRepositorio.listarPorModalidade(modalidade).stream()
            .filter(t -> t.getStatus() != StatusTemporada.CANCELADA)
            .anyMatch(nova::sobrepoeA);
        if (sobrepoe) {
            throw new IllegalStateException("RN37: ja existe temporada sobreposta para a modalidade");
        }
        return temporadaRepositorio.salvar(nova);
    }

    /** Cancela uma temporada antes de seu inicio (RN38). */
    public Temporada cancelar(int temporadaId, LocalDateTime agora) {
        Temporada temporada = obter(temporadaId);
        temporada.cancelar(agora);
        return temporadaRepositorio.salvar(temporada);
    }

    /** Encerra a temporada: snapshot final imutavel e soft-reset de PR (RN39). */
    public Temporada encerrar(int temporadaId, LocalDateTime agora) {
        Temporada temporada = obter(temporadaId);

        List<PontuacaoRanking> ranking = pontuacaoRepositorio.listarOrdenadasPorPr();
        List<EntradaSnapshot> snapshot = new ArrayList<>();
        for (int i = 0; i < ranking.size(); i++) {
            PontuacaoRanking p = ranking.get(i);
            snapshot.add(new EntradaSnapshot(p.getAtletaId(), i + 1, p.getPr()));
        }
        temporada.encerrar(snapshot);

        for (PontuacaoRanking p : ranking) {
            p.softReset(CompeticaoServico.PR_INICIAL, FATOR_SOFT_RESET);
            pontuacaoRepositorio.salvar(p);
        }
        return temporadaRepositorio.salvar(temporada);
    }

    private Temporada obter(int temporadaId) {
        return temporadaRepositorio.obterPorId(temporadaId)
            .orElseThrow(() -> new IllegalArgumentException("Temporada nao encontrada: " + temporadaId));
    }
}
