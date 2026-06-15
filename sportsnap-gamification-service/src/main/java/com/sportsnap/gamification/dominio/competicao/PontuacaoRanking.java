package com.sportsnap.gamification.dominio.competicao;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Pontuacao de Ranking (PR) de um atleta elegivel. O PR possui piso zero (RN32),
 * decai por inatividade (RN35) e e parcialmente reduzido no encerramento de
 * temporada (RN39). A liga (RN34) e derivada do PR atual.
 */
public class PontuacaoRanking {

    private final AtletaId atletaId;
    private double pr;
    private LocalDateTime ultimaPartida;

    public PontuacaoRanking(AtletaId atletaId, double prInicial) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        isTrue(prInicial >= 0, "O PR inicial nao pode ser negativo");
        this.atletaId = atletaId;
        this.pr = prInicial;
        this.ultimaPartida = null;
    }

    public PontuacaoRanking(AtletaId atletaId, double pr, LocalDateTime ultimaPartida) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        isTrue(pr >= 0, "O PR nao pode ser negativo");
        this.atletaId = atletaId;
        this.pr = pr;
        this.ultimaPartida = ultimaPartida;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public double getPr() {
        return pr;
    }

    public LocalDateTime getUltimaPartida() {
        return ultimaPartida;
    }

    public Liga getLiga() {
        return Liga.paraPr(pr);
    }

    /** Aplica a variacao de PR garantindo o piso zero (RN32). */
    public void aplicarVariacao(double delta) {
        this.pr = Math.max(0, pr + delta);
    }

    public void marcarPartida(LocalDateTime quando) {
        notNull(quando, "A data da partida nao pode ser nula");
        this.ultimaPartida = quando;
    }

    /** Decaimento percentual por inatividade (RN35). */
    public void aplicarDecaimento(double taxa) {
        isTrue(taxa >= 0 && taxa <= 1, "A taxa de decaimento deve estar entre 0 e 1");
        this.pr = Math.max(0, pr * (1 - taxa));
    }

    /** Soft-reset em direcao ao PR inicial no encerramento da temporada (RN39). */
    public void softReset(double prInicial, double fator) {
        isTrue(fator >= 0 && fator <= 1, "O fator de soft-reset deve estar entre 0 e 1");
        this.pr = Math.max(0, prInicial + (pr - prInicial) * fator);
    }
}
