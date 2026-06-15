package com.sportsnap.gamification.dominio.desafio;

import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sportsnap.gamification.dominio.atleta.AtletaId;

/**
 * Progresso de um atleta em um desafio aceito (RN23 a RN31). Mantem um contador
 * por criterio (limitado a meta, RN25), o status e a marca de insignia concedida
 * (idempotencia, RN28), alem do ciclo de referencia para desafios periodicos.
 */
public class ProgressoDesafio {

    private final Integer id;
    private final AtletaId atletaId;
    private final int desafioId;
    private final Map<Integer, Integer> contadores;
    private StatusProgresso status;
    private boolean insigniaConcedida;
    private LocalDate cicloReferencia;
    private final LocalDateTime iniciadoEm;

    public ProgressoDesafio(AtletaId atletaId, int desafioId, int numeroCriterios,
                            LocalDateTime iniciadoEm) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(iniciadoEm, "A data de inicio nao pode ser nula");
        this.id = null;
        this.atletaId = atletaId;
        this.desafioId = desafioId;
        this.contadores = new LinkedHashMap<>();
        for (int i = 0; i < numeroCriterios; i++) {
            contadores.put(i, 0); // RN23: progresso zerado
        }
        this.status = StatusProgresso.ATIVO;
        this.insigniaConcedida = false;
        this.cicloReferencia = iniciadoEm.toLocalDate();
        this.iniciadoEm = iniciadoEm;
    }

    public ProgressoDesafio(Integer id, AtletaId atletaId, int desafioId, Map<Integer, Integer> contadores,
                            StatusProgresso status, boolean insigniaConcedida,
                            LocalDate cicloReferencia, LocalDateTime iniciadoEm) {
        notNull(atletaId, "O id do Atleta nao pode ser nulo");
        notNull(status, "O status nao pode ser nulo");
        notNull(iniciadoEm, "A data de inicio nao pode ser nula");
        this.id = id;
        this.atletaId = atletaId;
        this.desafioId = desafioId;
        this.contadores = new LinkedHashMap<>(contadores);
        this.status = status;
        this.insigniaConcedida = insigniaConcedida;
        this.cicloReferencia = cicloReferencia;
        this.iniciadoEm = iniciadoEm;
    }

    public Integer getId() {
        return id;
    }

    public AtletaId getAtletaId() {
        return atletaId;
    }

    public int getDesafioId() {
        return desafioId;
    }

    public Map<Integer, Integer> getContadores() {
        return Collections.unmodifiableMap(contadores);
    }

    public StatusProgresso getStatus() {
        return status;
    }

    public boolean isInsigniaConcedida() {
        return insigniaConcedida;
    }

    public LocalDate getCicloReferencia() {
        return cicloReferencia;
    }

    public LocalDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public boolean estaAtivo() {
        return status == StatusProgresso.ATIVO;
    }

    /** Incrementa o contador de um criterio sem ultrapassar a meta (RN25). */
    public void incrementar(int criterioIndice, int quantidade, int meta) {
        if (status != StatusProgresso.ATIVO) {
            return;
        }
        int atual = contadores.getOrDefault(criterioIndice, 0);
        contadores.put(criterioIndice, Math.min(meta, atual + quantidade));
    }

    /** Define diretamente o contador de um criterio de limiar, limitado a meta (RN25). */
    public void registrarLimiarAtingido(int criterioIndice, boolean atingido, int meta) {
        if (status != StatusProgresso.ATIVO) {
            return;
        }
        contadores.put(criterioIndice, atingido ? meta : contadores.getOrDefault(criterioIndice, 0));
    }

    /** Conclusao ocorre quando todos os criterios atingem suas metas (RN26). */
    public boolean estaCompleto(List<Integer> metas) {
        for (int i = 0; i < metas.size(); i++) {
            if (contadores.getOrDefault(i, 0) < metas.get(i)) {
                return false;
            }
        }
        return true;
    }

    public void concluir() {
        this.status = StatusProgresso.CONCLUIDO;
    }

    public void expirar() {
        this.status = StatusProgresso.EXPIRADO;
    }

    /** Abandono descarta o progresso parcial (RN30). */
    public void cancelar() {
        this.status = StatusProgresso.CANCELADO;
    }

    public void marcarInsigniaConcedida() {
        this.insigniaConcedida = true;
    }

    /** Reinicia o ciclo de um desafio periodico, zerando o progresso (RN31). */
    public void reiniciarCiclo(LocalDate novoCiclo) {
        contadores.replaceAll((indice, valor) -> 0);
        this.cicloReferencia = novoCiclo;
        this.status = StatusProgresso.ATIVO;
        this.insigniaConcedida = false;
    }

    /** Percentual concluido considerando todas as metas (RN33). */
    public double percentualConcluido(List<Integer> metas) {
        int somaMetas = metas.stream().mapToInt(Integer::intValue).sum();
        if (somaMetas == 0) {
            return 100.0;
        }
        int somaContadores = 0;
        for (int i = 0; i < metas.size(); i++) {
            somaContadores += Math.min(metas.get(i), contadores.getOrDefault(i, 0));
        }
        return (double) somaContadores / somaMetas * 100.0;
    }

    /** Indices dos criterios ainda pendentes (RN33). */
    public List<Integer> criteriosPendentes(List<Integer> metas) {
        List<Integer> pendentes = new java.util.ArrayList<>();
        for (int i = 0; i < metas.size(); i++) {
            if (contadores.getOrDefault(i, 0) < metas.get(i)) {
                pendentes.add(i);
            }
        }
        return pendentes;
    }
}
