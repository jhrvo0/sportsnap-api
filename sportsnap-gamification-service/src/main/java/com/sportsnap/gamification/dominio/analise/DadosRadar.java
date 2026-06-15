package com.sportsnap.gamification.dominio.analise;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Dados do grafico radar com os atributos normalizados para escala comum (RN9). */
public class DadosRadar {

    private final Map<String, Double> valoresNormalizados;

    public DadosRadar(Map<String, Double> valoresNormalizados) {
        this.valoresNormalizados = new LinkedHashMap<>(valoresNormalizados);
    }

    public Map<String, Double> getValoresNormalizados() {
        return Collections.unmodifiableMap(valoresNormalizados);
    }
}
