package com.sportsnap.gamification.dominio.desafio;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Desafio (missao) definido pelo sistema (RN14 a RN18). Possui titulo, ao menos
 * um criterio, janela de validade ou marca de permanente, recompensa em insignia
 * propria deste contexto, pre-requisitos (cadeia) e cadencia (periodicidade).
 */
public class Desafio {

    private final Integer id;
    private final String titulo;
    private final List<CriterioDesafio> criterios;
    private final LocalDateTime inicio;
    private final LocalDateTime fim;
    private final boolean permanente;
    private final String insigniaCodigo;
    private final List<Integer> prerequisitos;
    private final Cadencia cadencia;
    private final boolean repetivel;

    public Desafio(String titulo, List<CriterioDesafio> criterios, LocalDateTime inicio,
                   LocalDateTime fim, boolean permanente, String insigniaCodigo,
                   List<Integer> prerequisitos, Cadencia cadencia, boolean repetivel) {
        this(null, titulo, criterios, inicio, fim, permanente, insigniaCodigo,
            prerequisitos, cadencia, repetivel);
    }

    public Desafio(Integer id, String titulo, List<CriterioDesafio> criterios, LocalDateTime inicio,
                   LocalDateTime fim, boolean permanente, String insigniaCodigo,
                   List<Integer> prerequisitos, Cadencia cadencia, boolean repetivel) {
        notBlank(titulo, "RN14: o desafio deve ter um titulo");
        notNull(criterios, "Os criterios nao podem ser nulos");
        notEmpty(criterios, "RN14: o desafio deve ter ao menos um criterio");
        notBlank(insigniaCodigo, "RN17: a recompensa em insignia e obrigatoria");
        notNull(cadencia, "A cadencia nao pode ser nula");
        if (!permanente) {
            notNull(inicio, "RN16: desafio nao permanente exige janela de validade");
            notNull(fim, "RN16: desafio nao permanente exige janela de validade");
            isTrue(fim.isAfter(inicio), "RN16: o fim deve ser posterior ao inicio");
        }
        this.id = id;
        this.titulo = titulo;
        this.criterios = new ArrayList<>(criterios);
        this.inicio = inicio;
        this.fim = fim;
        this.permanente = permanente;
        this.insigniaCodigo = insigniaCodigo;
        this.prerequisitos = new ArrayList<>(prerequisitos == null ? List.of() : prerequisitos);
        this.cadencia = cadencia;
        this.repetivel = repetivel;
    }

    public Integer getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<CriterioDesafio> getCriterios() {
        return Collections.unmodifiableList(criterios);
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public boolean isPermanente() {
        return permanente;
    }

    public String getInsigniaCodigo() {
        return insigniaCodigo;
    }

    public List<Integer> getPrerequisitos() {
        return Collections.unmodifiableList(prerequisitos);
    }

    public Cadencia getCadencia() {
        return cadencia;
    }

    public boolean isRepetivel() {
        return repetivel;
    }

    public List<Integer> metas() {
        return criterios.stream().map(CriterioDesafio::getMeta).toList();
    }

    /** O desafio aceita aceitacao/progresso no instante informado (RN20, RN27). */
    public boolean estaAtivoEm(LocalDateTime instante) {
        notNull(instante, "O instante nao pode ser nulo");
        if (permanente) {
            return true;
        }
        return !instante.isBefore(inicio) && !instante.isAfter(fim);
    }
}
