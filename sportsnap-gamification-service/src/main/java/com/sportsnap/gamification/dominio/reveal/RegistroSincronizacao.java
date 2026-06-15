package com.sportsnap.gamification.dominio.reveal;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Registro de uma Sincronizacao (Reveal) confirmada (RN23): guarda a data, o
 * orcamento liberado, a alocacao detalhada e a variacao de Overall.
 */
public class RegistroSincronizacao {

    private final Integer id;
    private final AtletaId atletaId;
    private final LocalDateTime ocorridoEm;
    private final int orcamentoPontos;
    private final int custoTotal;
    private final double overallAnterior;
    private final double overallNovo;
    private final Map<String, Integer> alocacao;

    public RegistroSincronizacao(AtletaId atletaId, LocalDateTime ocorridoEm, int orcamentoPontos,
                                 int custoTotal, double overallAnterior, double overallNovo,
                                 Map<String, Integer> alocacao) {
        this(null, atletaId, ocorridoEm, orcamentoPontos, custoTotal, overallAnterior, overallNovo, alocacao);
    }

    public RegistroSincronizacao(Integer id, AtletaId atletaId, LocalDateTime ocorridoEm,
                                 int orcamentoPontos, int custoTotal, double overallAnterior,
                                 double overallNovo, Map<String, Integer> alocacao) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(ocorridoEm, "A data do registro nao pode ser nula");
        notNull(alocacao, "A alocacao nao pode ser nula");
        this.id = id;
        this.atletaId = atletaId;
        this.ocorridoEm = ocorridoEm;
        this.orcamentoPontos = orcamentoPontos;
        this.custoTotal = custoTotal;
        this.overallAnterior = overallAnterior;
        this.overallNovo = overallNovo;
        this.alocacao = new LinkedHashMap<>(alocacao);
    }

    public Integer getId() {
        return id;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }

    public int getOrcamentoPontos() {
        return orcamentoPontos;
    }

    public int getCustoTotal() {
        return custoTotal;
    }

    public double getOverallAnterior() {
        return overallAnterior;
    }

    public double getOverallNovo() {
        return overallNovo;
    }

    public double getVariacaoOverall() {
        return overallNovo - overallAnterior;
    }

    public Map<String, Integer> getAlocacao() {
        return Collections.unmodifiableMap(alocacao);
    }
}
